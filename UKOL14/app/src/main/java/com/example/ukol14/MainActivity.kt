package com.example.ukol14

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ukol14.databinding.ActivityMainBinding
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: TaskAdapter
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "TODO Seznam"

        setupRecyclerView()
        loadTasks()

        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, AddTaskActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        adapter = TaskAdapter(
            onTaskClick = { task ->
                // CHYBĚJÍCÍ FUNKCE #2 - editace úkolu
                Toast.makeText(this, "Editace zatím není implementována", Toast.LENGTH_SHORT).show()
            },
            onTaskLongClick = { task ->
                showDeleteDialog(task)
            },
            onCheckboxClick = { task, isChecked ->
                updateTaskStatus(task.id, isChecked)
            }
        )

        binding.rvTasks.layoutManager = LinearLayoutManager(this)
        binding.rvTasks.adapter = adapter
    }

    private fun loadTasks() {
        db.collection("tasks")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(this, "Chyba: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                val tasks = snapshot?.documents?.mapNotNull { doc ->
                    Task(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        isCompleted = doc.getBoolean("isCompleted") ?: false
                    )
                } ?: emptyList()

                adapter.submitList(tasks)
            }
    }

    private fun updateTaskStatus(taskId: String, isCompleted: Boolean) {
        db.collection("tasks")
            .document(taskId)
            .update("isCompleted", isCompleted)
            .addOnFailureListener {
                Toast.makeText(this, "Chyba při aktualizaci", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showDeleteDialog(task: Task) {
        AlertDialog.Builder(this)
            .setTitle("Smazat úkol?")
            .setMessage("Opravdu chcete smazat \"${task.title}\"?")
            .setPositiveButton("Smazat") { _, _ ->
                deleteTask(task.id)
            }
            .setNegativeButton("Zrušit", null)
            .show()
    }

    private fun deleteTask(taskId: String) {
        db.collection("tasks")
            .document(taskId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Úkol smazán", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Chyba při mazání", Toast.LENGTH_SHORT).show()
            }
    }
}