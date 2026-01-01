package com.example.ukol14

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ukol14.databinding.ActivityAddTaskBinding
import com.google.firebase.firestore.FirebaseFirestore

class AddTaskActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddTaskBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Nový úkol"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.btnSave.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()

            if (title.isEmpty()) {
                Toast.makeText(this, "Vyplň název úkolu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val task = hashMapOf(
                "title" to title,
                "isCompleted" to false
            )

            db.collection("tasks")
                .add(task)
                .addOnSuccessListener {
                    Toast.makeText(this, "Úkol přidán", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Chyba: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}