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

class ActiveWorkoutAdapter(
    private val exercises: ArrayList<WorkoutExercise>,
    private val onDeleteExercise: (Int) -> Unit // Callback pro smazání celého cviku
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

        holder.tvName.text = exercise.name

        // 1. Vyčistit kontejner (protože RecyclerView recykluje pohledy)
        holder.setsContainer.removeAllViews()

        // 2. Vykreslit existující série
        exercise.sets.forEachIndexed { index, set ->
            addSetRow(holder.setsContainer, set, index, exercise)
        }

        // 3. Tlačítko "Přidat sérii"
        holder.btnAddSet.setOnClickListener {
            val newSet = WorkoutSet() // Prázdná série
            exercise.sets.add(newSet)
            addSetRow(holder.setsContainer, newSet, exercise.sets.size - 1, exercise)
        }

        // 4. Možnosti cviku (Smazání)
        holder.btnOptions.setOnClickListener {
            onDeleteExercise(holder.adapterPosition)
        }
    }

    // Funkce, která fyzicky vytvoří řádek se sérií
    private fun addSetRow(container: LinearLayout, set: WorkoutSet, index: Int, parentExercise: WorkoutExercise) {
        val inflater = LayoutInflater.from(container.context)
        val rowView = inflater.inflate(R.layout.item_set_row, container, false)

        val tvSetNumber = rowView.findViewById<TextView>(R.id.tvSetNumber)
        val etWeight = rowView.findViewById<EditText>(R.id.etWeight)
        val etReps = rowView.findViewById<EditText>(R.id.etReps)
        val btnDelete = rowView.findViewById<ImageButton>(R.id.btnDeleteSet)

        // Nastavení hodnot
        tvSetNumber.text = (index + 1).toString()
        if (set.weight > 0) etWeight.setText(set.weight.toString())
        if (set.reps > 0) etReps.setText(set.reps.toString())

        // --- LISTENERY PRO UKLÁDÁNÍ DAT (TextWatcher) ---
        // Musíme ukládat to, co uživatel píše, přímo do objektu 'set'

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

        // Smazání série
        btnDelete.setOnClickListener {
            parentExercise.sets.remove(set)
            container.removeView(rowView)
            // Přečíslování sérií (aby nebylo 1, 3, 4...)
            reorderSets(container)
        }

        container.addView(rowView)
    }

    private fun reorderSets(container: LinearLayout) {
        for (i in 0 until container.childCount) {
            val row = container.getChildAt(i)
            val tvNum = row.findViewById<TextView>(R.id.tvSetNumber)
            tvNum.text = (i + 1).toString()
        }
    }
}
