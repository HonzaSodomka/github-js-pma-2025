package com.example.ukol12

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ukol12.adapter.NoteAdapter
import com.example.ukol12.database.Note
import com.example.ukol12.database.NoteDatabase
import com.example.ukol12.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var noteAdapter: NoteAdapter
    private lateinit var database: NoteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = NoteDatabase.getDatabase(this)

        setupRecyclerView()
        observeNotes()
        supportActionBar?.title = "Moje poznámky"

        binding.fabAdd.setOnClickListener {
            val intent = Intent(this, AddEditNoteActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupRecyclerView() {
        noteAdapter = NoteAdapter(
            onItemClick = { note ->
                val intent = Intent(this, AddEditNoteActivity::class.java)
                intent.putExtra("NOTE_ID", note.id)
                startActivity(intent)
            },
            onItemLongClick = { note ->
                showDeleteDialog(note)
            }
        )

        binding.rvNotes.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = noteAdapter
        }
    }

    private fun observeNotes() {
        lifecycleScope.launch {
            database.noteDao().getAllNotes().collect { notes ->
                noteAdapter.submitList(notes)
            }
        }
    }

    private fun showDeleteDialog(note: Note) {
        AlertDialog.Builder(this)
            .setTitle("Smazat poznámku?")
            .setMessage("Opravdu chcete smazat poznámku \"${note.title}\"?")
            .setPositiveButton("Smazat") { _, _ ->
                lifecycleScope.launch {
                    database.noteDao().delete(note)
                }
            }
            .setNegativeButton("Zrušit", null)
            .show()
    }
}