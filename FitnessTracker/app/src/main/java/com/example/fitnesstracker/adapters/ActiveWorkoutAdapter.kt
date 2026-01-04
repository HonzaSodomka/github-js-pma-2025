package com.example.fitnesstracker.adapters

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnesstracker.R
import com.example.fitnesstracker.models.WorkoutExercise
import com.example.fitnesstracker.models.WorkoutSet
import com.example.fitnesstracker.utils.CustomExercise
import com.example.fitnesstracker.utils.ExerciseData

/**
 * Adapter pro aktivní trénink
 * - Detekuje kardio cviky a zobrazuje jiný layout (jen čas místo váhy+reps)
 * - Auto-save dat pomocí TextWatcher
 */
class ActiveWorkoutAdapter(
    private val exercises: ArrayList<WorkoutExercise>,
    private val customExercises: ArrayList<CustomExercise> = ArrayList(),
    private val onDeleteExercise: (Int) -> Unit
) : RecyclerView.Adapter<ActiveWorkoutAdapter.ExerciseViewHolder>() {

    class ExerciseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvExerciseName)
        val btnOptions: ImageButton = view.findViewById(R.id.btnExerciseOptions)
        val setsContainer: LinearLayout = view.findViewById(R.id.setsContainer)
        val btnAddSet: Button = view.findViewById(R.id.btnAddSet)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_workout_exercise, parent, false)
        return ExerciseViewHolder(view)
    }

    override fun getItemCount() = exercises.size

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        val exercise = exercises[position]
        val isCardio = isCardioExercise(exercise.name)

        holder.tvName.text = exercise.name

        // Vyčistit kontejner
        holder.setsContainer.removeAllViews()

        // Vykreslit existující série (s rozdílným layoutem pro kardio)
        exercise.sets.forEachIndexed { index, set ->
            if (isCardio) {
                addCardioSetRow(holder.setsContainer, set, index, exercise)
            } else {
                addSetRow(holder.setsContainer, set, index, exercise)
            }
        }

        // Tlačítko "Přidat sérii"
        holder.btnAddSet.setOnClickListener {
            val newSet = WorkoutSet()
            exercise.sets.add(newSet)

            if (isCardio) {
                addCardioSetRow(holder.setsContainer, newSet, exercise.sets.size - 1, exercise)
            } else {
                addSetRow(holder.setsContainer, newSet, exercise.sets.size - 1, exercise)
            }
        }

        // Možnosti cviku (Smazání)
        holder.btnOptions.setOnClickListener {
            onDeleteExercise(holder.adapterPosition)
        }
    }

    /**
     * Detekce zda je cvik kardio
     */
    private fun isCardioExercise(exerciseName: String): Boolean {
        val category = ExerciseData.getCategoryForExercise(exerciseName, customExercises)
        return category == "Kardio"
    }

    /**
     * Přidání normálního řádku série (váha + reps)
     */
    private fun addSetRow(container: LinearLayout, set: WorkoutSet, index: Int, parentExercise: WorkoutExercise) {
        val inflater = LayoutInflater.from(container.context)
        val rowView = inflater.inflate(R.layout.item_set_row, container, false)

        val tvSetNumber = rowView.findViewById<TextView>(R.id.tvSetNumber)
        val etWeight = rowView.findViewById<EditText>(R.id.etWeight)
        val etReps = rowView.findViewById<EditText>(R.id.etReps)
        val btnDelete = rowView.findViewById<ImageButton>(R.id.btnDeleteSet)

        tvSetNumber.text = (index + 1).toString()
        if (set.weight > 0) etWeight.setText(set.weight.toString())
        if (set.reps > 0) etReps.setText(set.reps.toString())

        // TextWatcher pro auto-save
        etWeight.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                set.weight = s.toString().toDoubleOrNull() ?: 0.0
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        etReps.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                set.reps = s.toString().toIntOrNull() ?: 0
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        btnDelete.setOnClickListener {
            parentExercise.sets.remove(set)
            container.removeView(rowView)
            reorderSets(container)
        }

        container.addView(rowView)
    }

    /**
     * Přidání kardio řádku série (jen čas v minutách)
     * - Čas se ukládá do pole "weight" (zneužití existujícího fieldu)
     */
    private fun addCardioSetRow(container: LinearLayout, set: WorkoutSet, index: Int, parentExercise: WorkoutExercise) {
        val inflater = LayoutInflater.from(container.context)
        val rowView = inflater.inflate(R.layout.item_set_row_cardio, container, false)

        val tvSetNumber = rowView.findViewById<TextView>(R.id.tvSetNumber)
        val etTime = rowView.findViewById<EditText>(R.id.etTime)
        val btnDelete = rowView.findViewById<ImageButton>(R.id.btnDeleteSet)

        tvSetNumber.text = (index + 1).toString()
        // Čas uložíme do weight fieldu (hack, ale funguje)
        if (set.weight > 0) etTime.setText(set.weight.toString())

        // TextWatcher pro auto-save času
        etTime.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                set.weight = s.toString().toDoubleOrNull() ?: 0.0
                set.reps = 0 // Pro kardio nepoužíváme reps
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        btnDelete.setOnClickListener {
            parentExercise.sets.remove(set)
            container.removeView(rowView)
            reorderSets(container)
        }

        container.addView(rowView)
    }

    private fun reorderSets(container: LinearLayout) {
        for (i in 0 until container.childCount) {
            val row = container.getChildAt(i)
            val tvNum = row.findViewById<TextView>(R.id.tvSetNumber)
            tvNum?.text = (i + 1).toString()
        }
    }
}