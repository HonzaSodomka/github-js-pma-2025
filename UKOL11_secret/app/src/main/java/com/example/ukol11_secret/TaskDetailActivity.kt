package com.example.ukol11_secret

import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ukol11_secret.databinding.ActivityTaskDetailBinding
import com.example.ukol11_secret.utils.TaskManager
import com.google.android.material.snackbar.Snackbar

class TaskDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTaskDetailBinding
    private lateinit var taskManager: TaskManager
    private var taskId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Detail úkolu"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        taskManager = TaskManager(this)

        taskId = intent.getStringExtra("TASK_ID")

        // Udělení dočasného oprávnění z intentu
        if (intent.clipData != null) {
            for (i in 0 until intent.clipData!!.itemCount) {
                val uri = intent.clipData!!.getItemAt(i).uri
                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        } else if (intent.data != null) {
            contentResolver.takePersistableUriPermission(intent.data!!, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        if (taskId != null) {
            loadTaskDetail(taskId!!)
        }

        binding.cbTaskCompleted.setOnCheckedChangeListener { _, isChecked ->
            if (taskId != null) {
                val task = taskManager.getTaskById(taskId!!)
                if (task != null && task.isCompleted != isChecked) {
                    taskManager.toggleTaskCompletion(taskId!!)
                    Toast.makeText(
                        this,
                        if (isChecked) "Úkol dokončen!" else "Úkol znovu aktivní",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        binding.btnDeleteTask.setOnClickListener {
            deleteTaskWithUndo()
        }
    }

    private fun loadTaskDetail(id: String) {
        val task = taskManager.getTaskById(id)

        if (task != null) {
            binding.tvDetailTitle.text = task.title
            binding.tvDetailCategory.text = task.category
            binding.tvDetailPriority.text = task.priority

            val priorityColor = when (task.priority) {
                "Vysoká" -> "#F44336"
                "Střední" -> "#FF9800"
                "Nízká" -> "#4CAF50"
                else -> "#666666"
            }
            binding.tvDetailPriority.setTextColor(android.graphics.Color.parseColor(priorityColor))

            binding.tvDetailDescription.text = if (task.description.isEmpty()) {
                "Bez popisu"
            } else {
                task.description
            }

            binding.tvDetailDeadline.text = if (task.deadline.isEmpty()) {
                "Bez termínu"
            } else {
                task.deadline
            }

            binding.cbTaskCompleted.isChecked = task.isCompleted

            if (task.imageUri != null) {
                try {
                    // Bezpečněji načíst obrázek přes InputStream
                    val uri = Uri.parse(task.imageUri)
                    val inputStream = contentResolver.openInputStream(uri)
                    val drawable = android.graphics.drawable.Drawable.createFromStream(inputStream, uri.toString())
                    binding.ivDetailImage.setImageDrawable(drawable)
                    inputStream?.close()
                } catch (e: SecurityException) {
                    Toast.makeText(this, "Nemáte oprávnění zobrazit obrázek.", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "Chyba při načítání obrázku.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun deleteTaskWithUndo() {
        if (taskId == null) return

        val task = taskManager.getTaskById(taskId!!)
        if (task == null) return

        taskManager.deleteTask(taskId!!)

        Snackbar.make(binding.root, "Úkol smazán", Snackbar.LENGTH_LONG)
            .setAction("Vrátit zpět") {
                taskManager.saveTask(task)
                loadTaskDetail(taskId!!)
                Toast.makeText(this, "Úkol obnoven", Toast.LENGTH_SHORT).show()
            }
            .show()

        binding.root.postDelayed({
            if (taskManager.getTaskById(taskId!!) == null) {
                finish()
            }
        }, 3000)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
