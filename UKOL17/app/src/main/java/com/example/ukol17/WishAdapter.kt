package com.example.ukol17

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ukol17.databinding.ItemWishBinding

class WishAdapter(
    private val onWishClick: (String) -> Unit,
    private val onWishLongClick: (String) -> Unit,
    private val onCheckboxClick: (String, Boolean) -> Unit
) : RecyclerView.Adapter<WishAdapter.WishViewHolder>() {

    private var wishes = listOf<String>()
    private var priorityWishes = setOf<String>()
    private var completedWishes = setOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WishViewHolder {
        val binding = ItemWishBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return WishViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WishViewHolder, position: Int) {
        holder.bind(wishes[position])
    }

    override fun getItemCount(): Int = wishes.size

    inner class WishViewHolder(private val binding: ItemWishBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(wish: String) {
            binding.tvWishName.text = wish

            val isPriority = priorityWishes.contains(wish)
            val isCompleted = completedWishes.contains(wish)

            // Zobraz/skryj hvězdičku
            binding.tvPriority.visibility = if (isPriority) View.VISIBLE else View.GONE

            // Nastav checkbox
            binding.cbCompleted.setOnCheckedChangeListener(null)
            binding.cbCompleted.isChecked = isCompleted

            // Přeškrtni text, pokud je splněno
            if (isCompleted) {
                binding.tvWishName.paintFlags = binding.tvWishName.paintFlags or
                        android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
                binding.tvWishName.alpha = 0.5f
            } else {
                binding.tvWishName.paintFlags = binding.tvWishName.paintFlags and
                        android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
                binding.tvWishName.alpha = 1f
            }

            // Kliknutí na přání (označit jako prioritní)
            binding.root.setOnClickListener {
                onWishClick(wish)
            }

            // Dlouhé kliknutí (smazat)
            binding.root.setOnLongClickListener {
                onWishLongClick(wish)
                true
            }

            // Checkbox
            binding.cbCompleted.setOnCheckedChangeListener { _, isChecked ->
                onCheckboxClick(wish, isChecked)
            }
        }
    }

    fun updateData(
        newWishes: List<String>,
        newPriority: Set<String>,
        newCompleted: Set<String>
    ) {
        wishes = newWishes
        priorityWishes = newPriority
        completedWishes = newCompleted
        notifyDataSetChanged()
    }
}