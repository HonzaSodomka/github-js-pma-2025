package com.example.ukolnicek.activities

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.ukolnicek.databinding.ActivityTaskDetailBinding
import com.example.ukolnicek.database.AppDatabase
import com.example.ukolnicek.models.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TaskDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTaskDetailBinding
    private var currentTask: Task? = null
    private var selectedDeadline: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTaskDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Ošetření výřezu
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Načtení dat pokud editujeme
        currentTask = intent.getParcelableExtra("task")

        setupUI()
    }

    private fun setupUI() {
        if (currentTask != null) {
            binding.tvTitle.text = "Upravit úkol"
            binding.etTitle.setText(currentTask!!.title)
            binding.etDescription.setText(currentTask!!.description)
            binding.sliderPriority.value = currentTask!!.priority.toFloat()
            binding.sliderDifficulty.value = currentTask!!.difficulty.toFloat()
            selectedDeadline = currentTask!!.deadlineTimestamp
            updateDateText()
            binding.btnDelete.visibility = View.VISIBLE
        } else {
            binding.tvTitle.text = "Nový úkol"
            binding.btnDelete.visibility = View.GONE
            selectedDeadline = System.currentTimeMillis() + 86400000 // Zítra
            updateDateText()
        }

        binding.btnDate.setOnClickListener {
            showDatePicker()
        }

        binding.btnSave.setOnClickListener {
            saveTask()
        }

        binding.btnDelete.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Smazat úkol?")
                .setMessage("Opravdu chcete smazat tento úkol?")
                .setPositiveButton("Smazat") { _, _ -> deleteTask() }
                .setNegativeButton("Zrušit", null)
                .show()
        }
        
        binding.btnBack.setOnClickListener { 
            finish() 
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        if (selectedDeadline > 0) {
            calendar.timeInMillis = selectedDeadline
        }

        DatePickerDialog(
            this,
            { _, year, month, day ->
                calendar.set(year, month, day)
                selectedDeadline = calendar.timeInMillis
                updateDateText()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateDateText() {
        val sdf = SimpleDateFormat("dd. MM. yyyy", Locale.getDefault())
        binding.tvDate.text = sdf.format(selectedDeadline)
    }

    private fun saveTask() {
        val title = binding.etTitle.text.toString().trim()
        val desc = binding.etDescription.text.toString().trim()
        val priority = binding.sliderPriority.value.toInt()
        val difficulty = binding.sliderDifficulty.value.toInt()

        if (title.isEmpty()) {
            binding.etTitle.error = "Vyplňte název"
            return
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        val taskToSave = currentTask?.copy(
            title = title,
            description = desc,
            priority = priority,
            difficulty = difficulty,
            deadlineTimestamp = selectedDeadline
        ) ?: Task(
            title = title,
            description = desc,
            priority = priority,
            difficulty = difficulty,
            deadlineTimestamp = selectedDeadline,
            userId = userId
        )

        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(applicationContext)
            
            if (currentTask == null) {
                db.taskDao().insertTask(taskToSave)
            } else {
                db.taskDao().updateTask(taskToSave)
            }

            // Sync to Firebase (Fire & Forget)
            if (userId.isNotEmpty()) {
                try {
                    val firestoreTask = hashMapOf(
                        "title" to title,
                        "description" to desc,
                        "priority" to priority,
                        "deadline" to selectedDeadline,
                        "isCompleted" to taskToSave.isCompleted
                    )
                    // Zjednodušeně: ukládáme jako nový dokument nebo update podle názvu (pro semestrálku stačí)
                    // V realitě bychom potřebovali ID dokumentu spárované s Room ID
                    FirebaseFirestore.getInstance()
                        .collection("users").document(userId)
                        .collection("tasks").add(firestoreTask)
                } catch (e: Exception) {
                    // Ignorujeme chyby sítě
                }
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(this@TaskDetailActivity, "Úkol uložen", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun deleteTask() {
        if (currentTask == null) return

        lifecycleScope.launch(Dispatchers.IO) {
            AppDatabase.getDatabase(applicationContext).taskDao().deleteTask(currentTask!!)
            withContext(Dispatchers.Main) {
                Toast.makeText(this@TaskDetailActivity, "Smazáno", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
