package com.example.ukolnicek.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ukolnicek.activities.TaskDetailActivity
import com.example.ukolnicek.adapters.TaskAdapter
import com.example.ukolnicek.databinding.FragmentCompletedTasksBinding
import com.example.ukolnicek.database.AppDatabase
import com.example.ukolnicek.models.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CompletedTasksFragment : Fragment() {

    private lateinit var binding: FragmentCompletedTasksBinding
    private lateinit var adapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCompletedTasksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadTasks()
    }

    private fun setupRecyclerView() {
        adapter = TaskAdapter(
            onTaskClick = { /* Nesplněné úkoly možná nejdou editovat, nebo ano? Necháme editaci */
                val intent = Intent(requireContext(), TaskDetailActivity::class.java)
                intent.putExtra("task", it)
                startActivity(intent)
            },
            onCheckClick = { task, isChecked ->
                updateTaskStatus(task, isChecked)
            }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun loadTasks() {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            db.taskDao().getCompletedTasks().collect { tasks ->
                adapter.submitList(tasks)
                binding.emptyState.visibility = if (tasks.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun updateTaskStatus(task: Task, isCompleted: Boolean) {
        lifecycleScope.launch(Dispatchers.IO) {
            val updatedTask = task.copy(isCompleted = isCompleted)
            AppDatabase.getDatabase(requireContext()).taskDao().updateTask(updatedTask)
        }
    }
}
