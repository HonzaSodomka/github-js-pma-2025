package com.example.fitnesstracker.adapters

import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnesstracker.R
import com.example.fitnesstracker.models.WorkoutExercise
import com.example.fitnesstracker.models.WorkoutSet
import com.example.fitnesstracker.utils.ExerciseData

/**
 * Adapter pro zobrazení cviků v detailu tréninku (READ-ONLY)
 * - Detekuje kardio cviky a zobrazuje jen čas místo váhy+reps
 */
class WorkoutDetailAdapter(
    private val exercises: List<WorkoutExercise>,
    private val onSetClick: (exercisePosition: Int, setPosition: Int, set: WorkoutSet) -> Unit // Nový callback
) : RecyclerView.Adapter<WorkoutDetailAdapter.ExerciseViewHolder>() {

    class ExerciseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvExerciseName: TextView = view.findViewById(R.id.tvExerciseName)
        val setsContainer: LinearLayout = view.findViewById(R.id.setsContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_workout_detail_exercise, parent, false)
        return ExerciseViewHolder(view)
    }

    override fun getItemCount() = exercises.size

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        val exercise = exercises[position]
        val isCardio = isCardioExercise(exercise.name)

        holder.tvExerciseName.text = exercise.name
        holder.setsContainer.removeAllViews()

        // Vykreslit série
        for ((setIndex, set) in exercise.sets.withIndex()) {
            val rowLayout = LinearLayout(holder.itemView.context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, 16, 0, 16)
                isClickable = true
                isFocusable = true
                // Efekt kliknutí (ripple)
                setBackgroundResource(android.R.attr.selectableItemBackground)

                // === KLIK LISTENER PRO EDITACI ===
                setOnClickListener {
                    onSetClick(position, setIndex, set)
                }
            }

            // Číslo série
            val tvNum = TextView(holder.itemView.context).apply {
                text = "${setIndex + 1}"
                setTextColor(Color.parseColor("#9CA3AF"))
                textSize = 14f
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                width = 60
            }
            rowLayout.addView(tvNum)

            // Hodnoty (rozdílné pro kardio vs normální)
            val tvValues = TextView(holder.itemView.context).apply {
                text = if (isCardio) {
                    // Pro kardio: jen čas v minutách
                    "${set.weight.toInt()} min"
                } else {
                    // Pro normální: váha × opakování
                    "${set.weight} kg   ×   ${set.reps}"
                }
                setTextColor(Color.parseColor("#374151"))
                textSize = 18f
                typeface = Typeface.DEFAULT_BOLD
                setPadding(32, 0, 0, 0)
            }
            rowLayout.addView(tvValues)

            holder.setsContainer.addView(rowLayout)
        }
    }

    /**
     * Detekce zda je cvik kardio
     * - Zkontroluje kategorii z ExerciseData
     */
    private fun isCardioExercise(exerciseName: String): Boolean {
        val category = ExerciseData.getCategoryForExercise(exerciseName, emptyList())
        return category == "Kardio"
    }
}