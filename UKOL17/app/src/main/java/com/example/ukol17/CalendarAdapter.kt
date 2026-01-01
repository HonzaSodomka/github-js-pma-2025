package com.example.ukol17

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.ukol17.databinding.ItemCalendarDayBinding

class CalendarAdapter(
    private val onDayClick: (Int) -> Unit,
    private val onCheckboxClick: (Int) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.DayViewHolder>() {

    private var openedDays = setOf<String>()
    private var completedDays = setOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val binding = ItemCalendarDayBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DayViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val day = position + 1  // Dny 1-24
        holder.bind(day)
    }

    override fun getItemCount(): Int = 24

    inner class DayViewHolder(private val binding: ItemCalendarDayBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(day: Int) {
            binding.tvDay.text = day.toString()

            val isOpened = openedDays.contains(day.toString())
            val isCompleted = completedDays.contains(day.toString())

            if (isOpened) {
                // Den je otevřený
                binding.tvLock.visibility = View.GONE
                binding.cbCompleted.visibility = View.VISIBLE

                // Nastav checkbox
                binding.cbCompleted.setOnCheckedChangeListener(null)
                binding.cbCompleted.isChecked = isCompleted
                binding.cbCompleted.setOnCheckedChangeListener { _, _ ->
                    onCheckboxClick(day)
                }

                // Barva podle stavu
                if (isCompleted) {
                    binding.root.setCardBackgroundColor(
                        ContextCompat.getColor(binding.root.context, android.R.color.holo_green_light)
                    )
                } else {
                    binding.root.setCardBackgroundColor(
                        ContextCompat.getColor(binding.root.context, android.R.color.white)
                    )
                }
            } else {
                // Den není otevřený
                binding.tvLock.visibility = View.VISIBLE
                binding.cbCompleted.visibility = View.GONE
                binding.root.setCardBackgroundColor(
                    ContextCompat.getColor(binding.root.context, android.R.color.darker_gray)
                )
            }

            // Kliknutí na políčko
            binding.root.setOnClickListener {
                if (!isOpened) {
                    onDayClick(day)
                }
            }
        }
    }

    fun updateData(opened: Set<String>, completed: Set<String>) {
        openedDays = opened
        completedDays = completed
        notifyDataSetChanged()
    }
}