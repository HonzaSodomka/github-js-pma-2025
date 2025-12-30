package com.example.ukol12

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.ukol12.database.Note
import com.example.ukol12.database.NoteDatabase
import com.example.ukol12.databinding.ActivityAddEditNoteBinding
import kotlinx.coroutines.launch

class AddEditNoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditNoteBinding
    private lateinit var database: NoteDatabase
    private var noteId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = NoteDatabase.getDatabase(this)

        noteId = intent.getIntExtra("NOTE_ID", -1)

        if (noteId != -1) {
            supportActionBar?.title = "Upravit"
        } else {
            supportActionBar?.title = "Nová poznámka"
        }

        binding.btnSave.setOnClickListener {
            saveNote()
        }
    }

    private fun loadNote() {
        lifecycleScope.launch {
            val note = database.noteDao().getNoteById(noteId)
            note?.let {
                binding.etTitle.setText(it.title)
                binding.etContent.setText(it.content)
            }
        }
    }

    private fun saveNote() {
        val title = binding.etTitle.text.toString()
        val content = binding.etContent.text.toString()

        if (title.isBlank()) {
            Toast.makeText(this, "Vyplňte název", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            if (noteId == -1) {
                val note = Note(title = title, content = content)
                database.noteDao().insert(note)
            } else {
                val note = Note(id = noteId, title = title, content = content)
                database.noteDao().update(note)
            }
            finish()
        }
    }
}