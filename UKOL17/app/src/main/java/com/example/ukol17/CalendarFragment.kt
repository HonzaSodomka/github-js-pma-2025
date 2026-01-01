package com.example.ukol17

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.ukol17.databinding.FragmentCalendarBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    private lateinit var dataStoreManager: DataStoreManager
    private lateinit var adapter: CalendarAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dataStoreManager = DataStoreManager(requireContext())

        setupRecyclerView()
        observeData()
    }

    private fun setupRecyclerView() {
        adapter = CalendarAdapter(
            onDayClick = { day ->
                openDay(day)
            },
            onCheckboxClick = { day ->
                viewLifecycleOwner.lifecycleScope.launch {
                    dataStoreManager.toggleDayCompleted(day)
                    loadData()
                }
            }
        )

        binding.rvCalendar.layoutManager = GridLayoutManager(requireContext(), 3)  // 3 sloupce
        binding.rvCalendar.adapter = adapter
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            dataStoreManager.openedDays.collect {
                loadData()
            }
        }
    }

    private fun loadData() {
        viewLifecycleOwner.lifecycleScope.launch {
            val opened = dataStoreManager.openedDays.first()
            val completed = dataStoreManager.completedDays.first()

            adapter.updateData(opened, completed)
        }
    }

    private fun openDay(day: Int) {
        val task = AdventTasks.getTaskByDay(day)
        if (task != null) {
            // Zobraz dialog s úkolem
            AlertDialog.Builder(requireContext())
                .setTitle("🎄 Den $day")
                .setMessage("${task.title}\n\n${task.description}")
                .setPositiveButton("Rozumím") { _, _ ->
                    // Ulož jako otevřený
                    viewLifecycleOwner.lifecycleScope.launch {
                        dataStoreManager.addOpenedDay(day)
                        loadData()
                    }
                }
                .setCancelable(false)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}