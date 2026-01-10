package com.example.ukolnicek.adapters

import android.content.res.ColorStateList
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ukolnicek.databinding.ItemTaskBinding
import com.example.ukolnicek.models.Task
import com.example.ukolnicek.utils.PriorityUtils
import java.text.SimpleDateFormat
import java.util.Locale

class TaskAdapter(
    private val onTaskClick: (Task) -> Unit,
    private val onCheckClick: (Task, Boolean) -> Unit
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = getItem(position)
        holder.bind(task)
    }

    inner class TaskViewHolder(private val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(task: Task) {
            binding.tvTitle.text = task.title
            
            // Priorita - barva a text
            val color = PriorityUtils.getPriorityColor(task.priority)
            binding.tvPriority.text = PriorityUtils.getPriorityText(task.priority)
            binding.tvPriority.setTextColor(color)
            binding.viewPriorityIndicator.backgroundTintList = ColorStateList.valueOf(color)

            // Datum
            val sdf = SimpleDateFormat("dd.MM.", Locale.getDefault())
            binding.tvDate.text = sdf.format(task.deadlineTimestamp)

            // Checkbox
            binding.cbCompleted.isChecked = task.isCompleted
            
            // Přeškrtnutí textu pokud je hotovo
            if (task.isCompleted) {
                binding.tvTitle.paintFlags = binding.tvTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                binding.tvTitle.alpha = 0.5f
            } else {
                binding.tvTitle.paintFlags = binding.tvTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                binding.tvTitle.alpha = 1.0f
            }

            // Click listeners
            binding.root.setOnClickListener { onTaskClick(task) }
            
            binding.cbCompleted.setOnClickListener { 
                onCheckClick(task, binding.cbCompleted.isChecked)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Task, newItem: Task) = oldItem == newItem
    }
}
