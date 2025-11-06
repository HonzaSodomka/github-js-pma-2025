package com.example.ukol11_secret.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.ukol11_secret.models.Task
import java.util.UUID

class TaskManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("TaskManagerPrefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_TASK_COUNT = "task_count"
        private const val PREFIX_TASK = "task_"
    }

    fun saveTask(task: Task) {
        val editor = prefs.edit()
        val prefix = "$PREFIX_TASK${task.id}_"

        editor.putString("${prefix}title", task.title)
        editor.putString("${prefix}description", task.description)
        editor.putString("${prefix}category", task.category)
        editor.putString("${prefix}priority", task.priority)
        editor.putString("${prefix}deadline", task.deadline)
        editor.putBoolean("${prefix}completed", task.isCompleted)
        editor.putString("${prefix}imageUri", task.imageUri)
        editor.putLong("${prefix}createdAt", task.createdAt)

        val taskIds = getTaskIds().toMutableSet()
        taskIds.add(task.id)
        editor.putStringSet("task_ids", taskIds)

        editor.apply()
    }

    fun getAllTasks(): List<Task> {
        val tasks = mutableListOf<Task>()
        val taskIds = getTaskIds()

        for (id in taskIds) {
            val task = getTaskById(id)
            if (task != null) {
                tasks.add(task)
            }
        }

        return tasks.sortedByDescending { it.createdAt }
    }

    fun getTaskById(id: String): Task? {
        val prefix = "$PREFIX_TASK${id}_"

        val title = prefs.getString("${prefix}title", null) ?: return null

        return Task(
            id = id,
            title = title,
            description = prefs.getString("${prefix}description", "") ?: "",
            category = prefs.getString("${prefix}category", "Osobní") ?: "Osobní",
            priority = prefs.getString("${prefix}priority", "Střední") ?: "Střední",
            deadline = prefs.getString("${prefix}deadline", "") ?: "",
            isCompleted = prefs.getBoolean("${prefix}completed", false),
            imageUri = prefs.getString("${prefix}imageUri", null),
            createdAt = prefs.getLong("${prefix}createdAt", System.currentTimeMillis())
        )
    }

    fun deleteTask(taskId: String) {
        val editor = prefs.edit()
        val prefix = "$PREFIX_TASK${taskId}_"

        editor.remove("${prefix}title")
        editor.remove("${prefix}description")
        editor.remove("${prefix}category")
        editor.remove("${prefix}priority")
        editor.remove("${prefix}deadline")
        editor.remove("${prefix}completed")
        editor.remove("${prefix}imageUri")
        editor.remove("${prefix}createdAt")

        val taskIds = getTaskIds().toMutableSet()
        taskIds.remove(taskId)
        editor.putStringSet("task_ids", taskIds)

        editor.apply()
    }

    fun toggleTaskCompletion(taskId: String) {
        val task = getTaskById(taskId) ?: return
        task.isCompleted = !task.isCompleted
        saveTask(task)
    }

    fun getActiveTasks(): List<Task> {
        return getAllTasks().filter { !it.isCompleted }
    }

    fun getCompletedTasks(): List<Task> {
        return getAllTasks().filter { it.isCompleted }
    }

    fun getTasksByCategory(category: String): List<Task> {
        return getAllTasks().filter { it.category == category }
    }

    fun getStatistics(): Map<String, Int> {
        val tasks = getAllTasks()
        return mapOf(
            "total" to tasks.size,
            "active" to tasks.count { !it.isCompleted },
            "completed" to tasks.count { it.isCompleted },
            "high_priority" to tasks.count { it.priority == "Vysoká" && !it.isCompleted }
        )
    }

    private fun getTaskIds(): Set<String> {
        return prefs.getStringSet("task_ids", emptySet()) ?: emptySet()
    }

    fun generateTaskId(): String {
        return UUID.randomUUID().toString()
    }
}