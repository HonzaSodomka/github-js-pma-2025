package com.example.ukolnicek.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ukolnicek.activities.TaskDetailActivity
import com.example.ukolnicek.adapters.TaskAdapter
import com.example.ukolnicek.databinding.FragmentActiveTasksBinding
import com.example.ukolnicek.database.AppDatabase
import com.example.ukolnicek.models.Task
import com.example.ukolnicek.utils.PreferencesManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ActiveTasksFragment : Fragment() {

    private lateinit var binding: FragmentActiveTasksBinding
    private lateinit var adapter: TaskAdapter
    private lateinit var prefs: PreferencesManager
    private var loadJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentActiveTasksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = PreferencesManager(requireContext())
        setupRecyclerView()
        
        binding.fabAdd.setOnClickListener {
            startActivity(Intent(requireContext(), TaskDetailActivity::class.java))
        }
    }
    
    // Načítáme úkoly při každém zobrazení (i návratu ze Settings)
    override fun onResume() {
        super.onResume()
        loadTasks()
    }

    fun refreshTasks() {
        loadTasks()
    }

    private fun setupRecyclerView() {
        adapter = TaskAdapter(
            onTaskClick = { task ->
                val intent = Intent(requireContext(), TaskDetailActivity::class.java)
                intent.putExtra("task", task)
                startActivity(intent)
            },
            onCheckClick = { task, isChecked ->
                updateTaskStatus(task, isChecked)
            }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        // Swipe to Delete
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val task = adapter.currentList[position]
                deleteTaskWithUndo(task)
            }
        }
        ItemTouchHelper(swipeHandler).attachToRecyclerView(binding.recyclerView)
    }

    private fun loadTasks() {
        // Zrušíme předchozí job, abychom neměli duplicitní collectory
        loadJob?.cancel()
        loadJob = lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            db.taskDao().getActiveTasks().collectLatest { tasks ->
                val sortedTasks = if (prefs.getSortOrder()) {
                    tasks.sortedBy { it.deadlineTimestamp }
                } else {
                    tasks.sortedByDescending { it.priority }
                }
                adapter.submitList(sortedTasks)
                binding.emptyState.visibility = if (sortedTasks.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun updateTaskStatus(task: Task, isCompleted: Boolean) {
        lifecycleScope.launch(Dispatchers.IO) {
            val updatedTask = task.copy(isCompleted = isCompleted)
            AppDatabase.getDatabase(requireContext()).taskDao().updateTask(updatedTask)
        }
    }

    private fun deleteTaskWithUndo(task: Task) {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(requireContext())
            db.taskDao().deleteTask(task)

            withContext(Dispatchers.Main) {
                Snackbar.make(binding.root, "Úkol smazán", Snackbar.LENGTH_LONG)
                    .setAction("VRÁTIT ZPĚT") {
                        lifecycleScope.launch(Dispatchers.IO) {
                            db.taskDao().insertTask(task)
                        }
                    }.show()
            }
        }
    }
}
