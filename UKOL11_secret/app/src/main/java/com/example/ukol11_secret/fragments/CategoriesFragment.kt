package com.example.ukol11_secret.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.ukol11_secret.databinding.FragmentCategoriesBinding
import com.example.ukol11_secret.utils.TaskManager

class CategoriesFragment : Fragment() {
    private var _binding: FragmentCategoriesBinding? = null
    private val binding get() = _binding!!
    private lateinit var taskManager: TaskManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        taskManager = TaskManager(requireContext())
        loadStatistics()
    }

    private fun loadStatistics() {
        val stats = taskManager.getStatistics()

        // Celkové statistiky
        binding.tvTotalTasks.text = "Celkem úkolů: ${stats["total"]}"
        binding.tvActiveTasks.text = "Aktivní: ${stats["active"]}"
        binding.tvCompletedTasks.text = "Dokončené: ${stats["completed"]}"
        binding.tvHighPriority.text = "Vysoká priorita: ${stats["high_priority"]}"

        // Statistiky podle kategorií
        val personalCount = taskManager.getTasksByCategory("Osobní").size
        val workCount = taskManager.getTasksByCategory("Práce").size
        val schoolCount = taskManager.getTasksByCategory("Škola").size

        binding.tvPersonalCount.text = "Osobní: $personalCount"
        binding.tvWorkCount.text = "Práce: $workCount"
        binding.tvSchoolCount.text = "Škola: $schoolCount"
    }

    override fun onResume() {
        super.onResume()
        loadStatistics()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}