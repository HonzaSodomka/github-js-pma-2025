package com.example.ukol11_secret.fragments

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.example.ukol11_secret.R
import com.example.ukol11_secret.TaskDetailActivity
import com.example.ukol11_secret.databinding.FragmentCompletedTasksBinding
import com.example.ukol11_secret.utils.TaskManager

class CompletedTasksFragment : Fragment() {
    private var _binding: FragmentCompletedTasksBinding? = null
    private val binding get() = _binding!!
    private lateinit var taskManager: TaskManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCompletedTasksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        taskManager = TaskManager(requireContext())
        loadCompletedTasks()
    }

    private fun loadCompletedTasks() {
        val completedTasks = taskManager.getCompletedTasks()

        binding.tvCompletedCount.text = "Dokončené úkoly: ${completedTasks.size}"

        binding.llCompletedTasksContainer.removeAllViews()

        if (completedTasks.isEmpty()) {
            val textView = TextView(requireContext())
            textView.text = "Žádné dokončené úkoly"
            textView.textSize = 16f
            textView.setTextColor(Color.GRAY)
            textView.setPadding(0, 32, 0, 0)
            binding.llCompletedTasksContainer.addView(textView)
        } else {
            for (task in completedTasks) {
                val cardView = layoutInflater.inflate(
                    R.layout.item_task,
                    binding.llCompletedTasksContainer,
                    false
                ) as CardView

                val tvTitle = cardView.findViewById<TextView>(R.id.tvTaskTitle)
                val tvPriority = cardView.findViewById<TextView>(R.id.tvTaskPriority)
                val tvCategory = cardView.findViewById<TextView>(R.id.tvTaskCategory)
                val tvDeadline = cardView.findViewById<TextView>(R.id.tvTaskDeadline)

                tvTitle.text = task.title
                tvPriority.text = task.priority
                tvCategory.text = task.category
                tvDeadline.text = if (task.deadline.isEmpty()) "Bez termínu" else task.deadline

                tvPriority.setTextColor(Color.parseColor("#4CAF50"))

                cardView.setOnClickListener {
                    val intent = Intent(requireContext(), TaskDetailActivity::class.java)
                    intent.putExtra("TASK_ID", task.id)
                    startActivity(intent)
                }

                binding.llCompletedTasksContainer.addView(cardView)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadCompletedTasks()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}