package com.example.fitnesstracker.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnesstracker.R
import com.example.fitnesstracker.models.Workout
import java.text.SimpleDateFormat
import java.util.Locale

class WorkoutHistoryAdapter(
    private val workouts: List<Workout>,
    private val onItemClick: (Workout) -> Unit,      // Funkce pro klik (Detail)
    private val onItemLongClick: (Workout) -> Unit   // Funkce pro podržení (Smazat)
) : RecyclerView.Adapter<WorkoutHistoryAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvHistoryName)
        val tvDate: TextView = view.findViewById(R.id.tvHistoryDate)
        val tvStats: TextView = view.findViewById(R.id.tvHistoryStats)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_workout_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val workout = workouts[position]

        holder.tvName.text = workout.name

        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        holder.tvDate.text = sdf.format(workout.date)

        val durationMin = workout.durationSeconds / 60
        holder.tvStats.text = "${workout.exercises.size} cviků • $durationMin min"

        // Kliknutí - Detail
        holder.itemView.setOnClickListener {
            onItemClick(workout)
        }

        // Dlouhé podržení - Smazat
        holder.itemView.setOnLongClickListener {
            onItemLongClick(workout)
            true // true znamená "zpracováno", aby se nespustil i obyčejný klik
        }
    }

    override fun getItemCount() = workouts.size
}
