package com.example.fitnesstracker.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnesstracker.R
import com.example.fitnesstracker.adapters.*
import com.example.fitnesstracker.models.Workout
import com.example.fitnesstracker.models.WorkoutExercise
import com.example.fitnesstracker.models.WorkoutSet
import com.example.fitnesstracker.utils.CustomExercise
import com.example.fitnesstracker.utils.ExerciseData
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Aktivita zobrazující detail dokončeného tréninku
 * - Obsahuje tlačítko UPRAVIT CVIKY, které aktivuje editační mód
 * - Umožňuje přidávat nové cviky (FAB) a mazat existující (koš)
 */
class WorkoutDetailActivity : AppCompatActivity() {

    private lateinit var workout: Workout
    private lateinit var adapter: WorkoutDetailAdapter
    private lateinit var btnShare: ImageButton
    private lateinit var btnEditMode: MaterialButton
    private lateinit var fabAddExercise: FloatingActionButton
    
    private var isModified = false 
    private var isEditMode = false
    private val customExercisesList = ArrayList<CustomExercise>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_workout_detail)

        val appBar = findViewById<AppBarLayout>(R.id.appBarLayout)
        ViewCompat.setOnApplyWindowInsetsListener(appBar) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        workout = intent.getParcelableExtra<Workout>("WORKOUT_DATA") ?: run {
            finish()
            return
        }

        val tvDetailName = findViewById<TextView>(R.id.tvDetailName)
        val tvDetailDate = findViewById<TextView>(R.id.tvDetailDate)
        val tvDetailDuration = findViewById<TextView>(R.id.tvDetailDuration)
        val rvExercises = findViewById<RecyclerView>(R.id.rvExercises)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        
        btnShare = findViewById(R.id.btnShare)
        btnEditMode = findViewById(R.id.btnEditMode)
        fabAddExercise = findViewById(R.id.fabAddExercise)

        // Vyplnění dat
        tvDetailName.text = workout.name
        val sdf = SimpleDateFormat("dd. MMM yyyy", Locale.getDefault())
        tvDetailDate.text = sdf.format(workout.date).uppercase()
        val minutes = workout.durationSeconds / 60
        tvDetailDuration.text = "$minutes MIN"

        // === SETUP ADAPTERU ===
        rvExercises.layoutManager = LinearLayoutManager(this)
        adapter = WorkoutDetailAdapter(
            workout.exercises,
            onSetClick = { exPos, setPos, set ->
                if (isEditMode) showEditSetDialog(exPos, setPos, set)
            },
            onDeleteExercise = { exPos ->
                showDeleteExerciseDialog(exPos)
            }
        )
        rvExercises.adapter = adapter

        // Načteme vlastní cviky pro případ přidávání
        loadCustomExercises()

        // === LISTENERS ===
        btnBack.setOnClickListener { finish() }
        
        btnShare.setOnClickListener { 
            if (isModified) saveChangesToFirestore() else shareWorkout() 
        }

        btnEditMode.setOnClickListener {
            toggleEditMode()
        }

        fabAddExercise.setOnClickListener {
            showCategorySelectionDialog()
        }
    }

    private fun toggleEditMode() {
        isEditMode = !isEditMode
        
        if (isEditMode) {
            btnEditMode.text = "UKONČIT ÚPRAVY"
            btnEditMode.setBackgroundColor(Color.parseColor("#EF4444")) // Červená
            fabAddExercise.visibility = View.VISIBLE // Ukázat tlačítko přidat
        } else {
            btnEditMode.text = "UPRAVIT CVIKY"
            btnEditMode.setBackgroundColor(Color.parseColor("#4F46E5")) // Modrá
            fabAddExercise.visibility = View.GONE
        }

        adapter.setEditMode(isEditMode)
    }

    // === MAZÁNÍ CVIKU ===
    private fun showDeleteExerciseDialog(position: Int) {
        val exerciseName = workout.exercises[position].name
        AlertDialog.Builder(this)
            .setTitle("Smazat cvik?")
            .setMessage("Opravdu chceš odstranit $exerciseName?")
            .setPositiveButton("Smazat") { _, _ ->
                workout.exercises.removeAt(position)
                adapter.notifyItemRemoved(position)
                adapter.notifyItemRangeChanged(position, workout.exercises.size)
                enableSaveMode()
            }
            .setNegativeButton("Zrušit", null)
            .show()
    }

    // === PŘIDÁVÁNÍ NOVÉHO CVIKU (Kód převzatý a upravený z ActiveWorkoutActivity) ===
    
    private fun loadCustomExercises() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("users").document(userId).collection("custom_exercises")
            .get()
            .addOnSuccessListener { result ->
                customExercisesList.clear()
                for (document in result) {
                    val name = document.getString("name") ?: ""
                    val category = document.getString("category") ?: ""
                    if (name.isNotEmpty()) customExercisesList.add(CustomExercise(name, category))
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
                if (arr[which] == "+ Vytvořit nový cvik") {
                    showCreateCustomExerciseDialog(category)
                } else {
                    addNewExercise(arr[which])
                }
            }
            .setNegativeButton("Zpět") { _, _ -> showCategorySelectionDialog() }
            .show()
    }

    private fun showCreateCustomExerciseDialog(category: String) {
        val input = EditText(this)
        input.hint = "Název cviku"
        val container = FrameLayout(this)
        val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.leftMargin = 50
        params.rightMargin = 50
        input.layoutParams = params
        container.addView(input)

        AlertDialog.Builder(this)
            .setTitle("Nový cvik - $category")
            .setView(container)
            .setPositiveButton("Uložit") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    // Jen lokálně přidáme do listu pro příště, ukládat do DB budeme až s celým workoutem nebo separátně
                    // Pro jednoduchost zde:
                    addNewExercise(name)
                }
            }
            .setNegativeButton("Zrušit", null)
            .show()
    }

    private fun addNewExercise(name: String) {
        // Přidáme nový cvik s jednou prázdnou sérií, aby šel hned editovat
        val newEx = WorkoutExercise(name = name)
        newEx.sets.add(WorkoutSet(0.0, 0, true)) 
        
        workout.exercises.add(newEx)
        adapter.notifyItemInserted(workout.exercises.size - 1)
        enableSaveMode()
        
        Toast.makeText(this, "Cvik přidán na konec seznamu", Toast.LENGTH_SHORT).show()
    }

    // === EDITACE SÉRII ===
    private fun showEditSetDialog(exPos: Int, setPos: Int, set: WorkoutSet) {
        val context = this
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 40, 60, 10)
        }

        val inputWeight = EditText(context).apply {
            hint = "Váha (kg)"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText(set.weight.toString())
        }
        
        val inputReps = EditText(context).apply {
            hint = "Opakování"
            inputType = InputType.TYPE_CLASS_NUMBER
            setText(set.reps.toString())
        }

        layout.addView(inputWeight)
        layout.addView(inputReps)

        AlertDialog.Builder(context)
            .setTitle("Upravit sérii ${setPos + 1}")
            .setView(layout)
            .setPositiveButton("Uložit") { _, _ ->
                val newWeight = inputWeight.text.toString().toDoubleOrNull() ?: set.weight
                val newReps = inputReps.text.toString().toIntOrNull() ?: set.reps

                workout.exercises[exPos].sets[setPos].weight = newWeight
                workout.exercises[exPos].sets[setPos].reps = newReps
                
                adapter.notifyItemChanged(exPos)
                enableSaveMode()
            }
            .setNegativeButton("Zrušit", null)
            .show()
    }

    private fun enableSaveMode() {
        if (!isModified) {
            isModified = true
            btnShare.setImageResource(android.R.drawable.ic_menu_save)
            Toast.makeText(this, "Změny provedeny. Nezapomeň uložit!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveChangesToFirestore() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        if (workout.id.isEmpty()) return

        btnShare.isEnabled = false
        btnEditMode.isEnabled = false

        FirebaseFirestore.getInstance().collection("users").document(userId)
            .collection("workouts").document(workout.id)
            .set(workout)
            .addOnSuccessListener {
                Toast.makeText(this, "Uloženo!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Chyba: ${e.message}", Toast.LENGTH_LONG).show()
                btnShare.isEnabled = true
                btnEditMode.isEnabled = true
            }
    }

    private fun shareWorkout() {
        // ... (stejné jako předtím)
        val sdf = SimpleDateFormat("dd. MMM yyyy", Locale.getDefault())
        val dateStr = sdf.format(workout.date)
        val builder = StringBuilder()
        builder.append("💪 ${workout.name}\n📅 $dateStr\n\n")
        workout.exercises.forEachIndexed { i, ex ->
            builder.append("${i + 1}. ${ex.name}\n")
            ex.sets.forEach { s -> builder.append("   ${s.weight}kg × ${s.reps}\n") }
            builder.append("\n")
        }
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, builder.toString())
        }
        startActivity(Intent.createChooser(shareIntent, "Sdílet"))
    }
}