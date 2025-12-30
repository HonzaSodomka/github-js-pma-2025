package com.example.ukol13.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ukol13.database.GameResult
import com.example.ukol13.databinding.ItemGameResultBinding
import java.text.SimpleDateFormat
import java.util.*

class GameResultAdapter(
    private val categoryNames: Map<Int, String>
) : ListAdapter<GameResult, GameResultAdapter.GameResultViewHolder>(GameResultDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameResultViewHolder {
        val binding = ItemGameResultBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return GameResultViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GameResultViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class GameResultViewHolder(private val binding: ItemGameResultBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(result: GameResult) {
            binding.tvCategoryName.text = categoryNames[result.categoryId] ?: "Neznámá"
            binding.tvScore.text = "${result.score}/${result.totalQuestions}"

            val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            binding.tvDate.text = dateFormat.format(Date(result.timestamp))
        }
    }

    class GameResultDiffCallback : DiffUtil.ItemCallback<GameResult>() {
        override fun areItemsTheSame(oldItem: GameResult, newItem: GameResult): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: GameResult, newItem: GameResult): Boolean {
            return oldItem == newItem
        }
    }
}