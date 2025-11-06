package com.example.ukol11_secret

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.ukol11_secret.databinding.ActivityAddTaskBinding
import com.example.ukol11_secret.models.Task
import com.example.ukol11_secret.utils.TaskManager

class AddTaskActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddTaskBinding
    private lateinit var taskManager: TaskManager
    private var selectedImageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            binding.ivTaskImage.setImageURI(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Přidat úkol"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        taskManager = TaskManager(this)

        binding.btnPickImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnSaveTask.setOnClickListener {
            saveTask()
        }
    }

    private fun saveTask() {
        val title = binding.etTaskTitle.text.toString()
        val description = binding.etTaskDescription.text.toString()
        val deadline = binding.etDeadline.text.toString()

        if (title.isEmpty()) {
            Toast.makeText(this, "Zadejte název úkolu", Toast.LENGTH_SHORT).show()
            return
        }

        val category = when (binding.rgCategory.checkedRadioButtonId) {
            R.id.rbPersonal -> "Osobní"
            R.id.rbWork -> "Práce"
            R.id.rbSchool -> "Škola"
            else -> "Osobní"
        }

        val priority = when (binding.rgPriority.checkedRadioButtonId) {
            R.id.rbLowPriority -> "Nízká"
            R.id.rbMediumPriority -> "Střední"
            R.id.rbHighPriority -> "Vysoká"
            else -> "Střední"
        }

        val task = Task(
            id = taskManager.generateTaskId(),
            title = title,
            description = description,
            category = category,
            priority = priority,
            deadline = deadline,
            imageUri = selectedImageUri?.toString()
        )

        taskManager.saveTask(task)

        Toast.makeText(this, "Úkol přidán!", Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}