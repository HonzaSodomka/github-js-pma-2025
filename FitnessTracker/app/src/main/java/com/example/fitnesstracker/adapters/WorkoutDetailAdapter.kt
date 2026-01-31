package com.example.fitnesstracker.adapters

import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnesstracker.R
import com.example.fitnesstracker.models.WorkoutExercise
import com.example.fitnesstracker.models.WorkoutSet
import com.example.fitnesstracker.utils.ExerciseData

/**
 * Adapter pro zobrazení cviků v detailu tréninku
 * - Podporuje "Edit Mode":
 *   - Editace sérií (klik na řádek)
 *   - Mazání celých cviků (tlačítko koše)
 */
class WorkoutDetailAdapter(
    private val exercises: List<WorkoutExercise>,
    private val onSetClick: (exercisePosition: Int, setPosition: Int, set: WorkoutSet) -> Unit,
    private val onDeleteExercise: (exercisePosition: Int) -> Unit // Nový callback pro smazání
) : RecyclerView.Adapter<WorkoutDetailAdapter.ExerciseViewHolder>() {

    private var isEditMode = false

    class ExerciseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvExerciseName: TextView = view.findViewById(R.id.tvExerciseName)
        val setsContainer: LinearLayout = view.findViewById(R.id.setsContainer)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDeleteExercise) // Tlačítko smazat
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_workout_detail_exercise, parent, false)
        return ExerciseViewHolder(view)
    }

    override fun getItemCount() = exercises.size

    fun setEditMode(enabled: Boolean) {
        isEditMode = enabled
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        val exercise = exercises[position]
        val isCardio = isCardioExercise(exercise.name)

        holder.tvExerciseName.text = exercise.name
        
        // Zobrazit/Skrýt tlačítko smazání podle edit módu
        holder.btnDelete.visibility = if (isEditMode) View.VISIBLE else View.GONE
        holder.btnDelete.setOnClickListener {
            onDeleteExercise(position)
        }

        holder.setsContainer.removeAllViews()

        for ((setIndex, set) in exercise.sets.withIndex()) {
            val rowLayout = LinearLayout(holder.itemView.context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, 16, 0, 16)
                
                if (isEditMode) {
                    setBackgroundColor(Color.parseColor("#F3F4F6"))
                } else {
                    setBackgroundColor(Color.TRANSPARENT)
                }

                setOnClickListener {
                    onSetClick(position, setIndex, set)
                }
            }

            val tvNum = TextView(holder.itemView.context).apply {
                text = "${setIndex + 1}"
                setTextColor(Color.parseColor("#9CA3AF"))
                textSize = 14f
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                width = 60
            }
            rowLayout.addView(tvNum)

            val tvValues = TextView(holder.itemView.context).apply {
                text = if (isCardio) {
                    "${set.weight.toInt()} min"
                } else {
                    "${set.weight} kg   ×   ${set.reps}"
                }
                setTextColor(Color.parseColor("#374151"))
                textSize = 18f
                typeface = Typeface.DEFAULT_BOLD
                setPadding(32, 0, 0, 0)
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            }
            rowLayout.addView(tvValues)

            if (isEditMode) {
                val tvEditIcon = TextView(holder.itemView.context).apply {
                    text = "✏️"
                    textSize = 16f
                    setPadding(16, 0, 32, 0)
                    gravity = Gravity.CENTER
                }
                rowLayout.addView(tvEditIcon)
            }

            holder.setsContainer.addView(rowLayout)
        }
    }

    private fun isCardioExercise(exerciseName: String): Boolean {
        val category = ExerciseData.getCategoryForExercise(exerciseName, emptyList())
        return category == "Kardio"
    }
}