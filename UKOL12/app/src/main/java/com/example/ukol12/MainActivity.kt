package com.example.ukol12

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.ukol12.database.Note
import com.example.ukol12.database.NoteDao
import com.example.ukol12.database.NoteHubDatabaseInstance
import com.example.ukol12.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var noteDao: NoteDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ViewBinding setup
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Database setup
        val database = NoteHubDatabaseInstance.getDatabase(this)
        noteDao = database.noteDao()

        // Tlačítko pro přidání poznámky
        binding.btnAddNote.setOnClickListener {
            showAddNoteDialog()
        }

        // Načti a zobraz poznámky
        loadNotes()
    }

    private fun showAddNoteDialog(noteToEdit: Note? = null) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_note, null)
        val etTitle = dialogView.findViewById<EditText>(R.id.etTitle)
        val etDescription = dialogView.findViewById<EditText>(R.id.etDescription)

        // Pokud editujeme poznámku, předvyplň pole
        noteToEdit?.let {
            etTitle.setText(it.title)
            etDescription.setText(it.description)
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialogView.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btnSave).setOnClickListener {
            val title = etTitle.text.toString().trim()
            val description = etDescription.text.toString().trim()

            if (title.isNotEmpty() && description.isNotEmpty()) {
                if (noteToEdit == null) {
                    // Přidat novou poznámku
                    val newNote = Note(title = title, description = description)
                    lifecycleScope.launch {
                        noteDao.insert(newNote)
                    }
                } else {
                    // Aktualizovat existující poznámku
                    val updatedNote = noteToEdit.copy(title = title, description = description)
                    lifecycleScope.launch {
                        noteDao.update(updatedNote)
                    }
                }
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun loadNotes() {
        lifecycleScope.launch {
            noteDao.getAllNotes().collect { notes ->
                displayNotes(notes)
            }
        }
    }

    private fun displayNotes(notes: List<Note>) {
        binding.llNotesList.removeAllViews()

        for (note in notes) {
            val noteView = LayoutInflater.from(this).inflate(
                android.R.layout.simple_list_item_2,
                binding.llNotesList,
                false
            )

            noteView.findViewById<TextView>(android.R.id.text1).text = note.title
            noteView.findViewById<TextView>(android.R.id.text2).text = note.description

            // Kliknutí = editace
            noteView.setOnClickListener {
                showAddNoteDialog(note)
            }

            // Dlouhé kliknutí = smazání
            noteView.setOnLongClickListener {
                showDeleteDialog(note)
                true
            }

            binding.llNotesList.addView(noteView)
        }
    }

    private fun showDeleteDialog(note: Note) {
        AlertDialog.Builder(this)
            .setTitle("Smazat poznámku?")
            .setMessage("Opravdu chcete smazat poznámku \"${note.title}\"?")
            .setPositiveButton("Ano") { _, _ ->
                lifecycleScope.launch {
                    noteDao.delete(note)
                }
            }
            .setNegativeButton("Ne", null)
            .show()
    }
}
