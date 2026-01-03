package com.example.fitnesstracker

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnesstracker.adapters.ActiveWorkoutAdapter
import com.example.fitnesstracker.models.Workout
import com.example.fitnesstracker.models.WorkoutExercise
import com.example.fitnesstracker.utils.CustomExercise
import com.example.fitnesstracker.utils.ExerciseData
import com.google.android.material.appbar.AppBarLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class ActiveWorkoutActivity : AppCompatActivity() {

    private lateinit var rvExercises: RecyclerView
    private lateinit var adapter: ActiveWorkoutAdapter
    private lateinit var btnAddExercise: Button
    private lateinit var btnFinish: Button
    private lateinit var btnClose: TextView // ZMĚNA ZDE: TextView místo ImageButton
    private lateinit var etWorkoutName: EditText
    private lateinit var tvTimer: TextView
    private lateinit var appBarLayout: AppBarLayout

    private val currentWorkout = Workout()
    private var startTime: Long = 0
    private val customExercisesList = ArrayList<CustomExercise>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_active_workout)

        appBarLayout = findViewById(R.id.appBarLayout)

        ViewCompat.setOnApplyWindowInsetsListener(appBarLayout) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        // Setup UI
        rvExercises = findViewById(R.id.rvExercises)
        btnAddExercise = findViewById(R.id.btnAddExercise)
        btnFinish = findViewById(R.id.btnFinish)
        btnClose = findViewById(R.id.btnClose) // Najde TextView
        etWorkoutName = findViewById(R.id.etWorkoutName)
        tvTimer = findViewById(R.id.tvTimer)

        startTime = System.currentTimeMillis()
        startTimerDisplay()

        loadCustomExercises()

        rvExercises.layoutManager = LinearLayoutManager(this)
        adapter = ActiveWorkoutAdapter(currentWorkout.exercises) { position ->
            deleteExercise(position)
        }
        rvExercises.adapter = adapter

        btnAddExercise.setOnClickListener {
            showCategorySelectionDialog()
        }

        btnFinish.setOnClickListener {
            finishWorkout()
        }

        btnClose.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Zrušit trénink?")
                .setMessage("Všechna data budou ztracena.")
                .setPositiveButton("Zrušit") { _, _ -> finish() }
                .setNegativeButton("Pokračovat", null)
                .show()
        }
    }

    private fun loadCustomExercises() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(userId).collection("custom_exercises")
            .get()
            .addOnSuccessListener { result ->
                customExercisesList.clear()
                for (document in result) {
                    val name = document.getString("name") ?: ""
                    val category = document.getString("category") ?: ""
                    if (name.isNotEmpty()) {
                        customExercisesList.add(CustomExercise(name, category))
                    }
                }
            }
    }

    private fun showCategorySelectionDialog() {
        val categories = ExerciseData.categories.keys.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Vyber partii")
            .setItems(categories) { _, which ->
                showExerciseSelectionDialog(categories[which])
            }
            .setNegativeButton("Zrušit", null)
            .show()
    }

    private fun showExerciseSelectionDialog(category: String) {
        val exercises = ExerciseData.categories[category]?.toMutableList() ?: mutableListOf()
        val myCustom = customExercisesList.filter { it.category == category }
        for (c in myCustom) exercises.add(c.name)
        exercises.sort()
        exercises.add(0, "+ Vytvořit nový cvik")

        val arr = exercises.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle(category)
            .setItems(arr) { _, which ->
                if (arr[which] == "+ Vytvořit nový cvik") showCreateCustomExerciseDialog(category)
                else addNewExercise(arr[which])
            }
            .setNegativeButton("Zpět") { _, _ -> showCategorySelectionDialog() }
            .show()
    }

    private fun showCreateCustomExerciseDialog(category: String) {
        val input = EditText(this)
        input.hint = "Název cviku"
        val container = android.widget.FrameLayout(this)
        val params = android.widget.FrameLayout.LayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.leftMargin = 50
        params.rightMargin = 50
        input.layoutParams = params
        container.addView(input)

        AlertDialog.Builder(this)
            .setTitle("Nový cvik - $category")
            .setView(container)
            .setPositiveButton("Uložit") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) saveCustomExerciseToFirestore(name, category)
            }
            .setNegativeButton("Zrušit", null)
            .show()
    }

    private fun saveCustomExerciseToFirestore(name: String, category: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("users").document(userId)
            .collection("custom_exercises").add(hashMapOf("name" to name, "category" to category))
            .addOnSuccessListener {
                Toast.makeText(this, "Cvik uložen", Toast.LENGTH_SHORT).show()
                customExercisesList.add(CustomExercise(name, category))
                addNewExercise(name)
            }
    }

    private fun addNewExercise(name: String) {
        currentWorkout.exercises.add(WorkoutExercise(name = name))
        adapter.notifyItemInserted(currentWorkout.exercises.size - 1)
        rvExercises.scrollToPosition(currentWorkout.exercises.size - 1)
    }

    private fun deleteExercise(position: Int) {
        currentWorkout.exercises.removeAt(position)
        adapter.notifyItemRemoved(position)
    }

    private fun finishWorkout() {
        if (currentWorkout.exercises.isEmpty()) {
            Toast.makeText(this, "Trénink je prázdný", Toast.LENGTH_SHORT).show()
            return
        }
        val currentName = etWorkoutName.text.toString()
        if (currentName == "Dnešní trénink" || currentName.isEmpty()) {
            val cats = mutableSetOf<String>()
            for (ex in currentWorkout.exercises) {
                val c = ExerciseData.getCategoryForExercise(ex.name, customExercisesList)
                if (c != "Ostatní") cats.add(c)
            }
            currentWorkout.name = if (cats.isNotEmpty()) cats.joinToString(" & ") else "Full Body"
        } else {
            currentWorkout.name = currentName
        }
        currentWorkout.date = Date()
        currentWorkout.durationSeconds = (System.currentTimeMillis() - startTime) / 1000

        btnFinish.isEnabled = false
        btnFinish.text = "UKLÁDÁM..."

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("users").document(userId)
            .collection("workouts").add(currentWorkout)
            .addOnSuccessListener {
                Toast.makeText(this, "Uloženo!", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener {
                btnFinish.isEnabled = true
                btnFinish.text = "DOKONČIT"
                Toast.makeText(this, "Chyba: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun startTimerDisplay() {
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        handler.post(object : Runnable {
            override fun run() {
                val millis = System.currentTimeMillis() - startTime
                val sec = (millis / 1000) % 60
                val min = (millis / (1000 * 60)) % 60
                val hr = (millis / (1000 * 60 * 60))
                tvTimer.text = if (hr > 0) String.format("%d:%02d:%02d", hr, min, sec)
                else String.format("%02d:%02d", min, sec)
                handler.postDelayed(this, 1000)
            }
        })
    }
}
