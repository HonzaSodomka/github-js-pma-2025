package com.example.fitnesstracker.activities

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
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
import com.example.fitnesstracker.models.WorkoutSet
import com.google.android.material.appbar.AppBarLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Aktivita zobrazující detail dokončeného tréninku
 * - Název, datum, délka tréninku
 * - Seznam cviků s jejich sériemi (RecyclerView)
 * - Možnost editace sérií (kliknutím) a následné uložení
 * - Možnost sdílení tréninku jako text (pokud nejsou neuložené změny)
 * - Edge-to-edge design
 */
class WorkoutDetailActivity : AppCompatActivity() {

    private lateinit var workout: Workout
    private lateinit var adapter: WorkoutDetailAdapter
    private lateinit var btnShare: ImageButton
    private var isModified = false // Sledujeme, jestli došlo ke změně

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
        btnShare = findViewById<ImageButton>(R.id.btnShare)

        // Vyplnění dat
        tvDetailName.text = workout.name
        val sdf = SimpleDateFormat("dd. MMM yyyy", Locale.getDefault())
        tvDetailDate.text = sdf.format(workout.date).uppercase()
        val minutes = workout.durationSeconds / 60
        tvDetailDuration.text = "$minutes MIN"

        // === SETUP ADAPTERU S CLICK LISTENEREM ===
        rvExercises.layoutManager = LinearLayoutManager(this)
        adapter = WorkoutDetailAdapter(workout.exercises) { exPos, setPos, set ->
            showEditSetDialog(exPos, setPos, set)
        }
        rvExercises.adapter = adapter

        btnBack.setOnClickListener { finish() }
        
        // Tlačítko Share funguje primárně jako Sdílet, 
        // ale pokud uděláme změnu, změní se na Uložit
        btnShare.setOnClickListener { 
            if (isModified) {
                saveChangesToFirestore()
            } else {
                shareWorkout() 
            }
        }
    }

    /**
     * Zobrazí dialog pro editaci váhy a opakování
     */
    private fun showEditSetDialog(exPos: Int, setPos: Int, set: WorkoutSet) {
        val context = this
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
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
            .setTitle("Upravit sérii")
            .setView(layout)
            .setPositiveButton("OK") { _, _ ->
                val newWeight = inputWeight.text.toString().toDoubleOrNull() ?: set.weight
                val newReps = inputReps.text.toString().toIntOrNull() ?: set.reps

                // Aplikování změny
                workout.exercises[exPos].sets[setPos].weight = newWeight
                workout.exercises[exPos].sets[setPos].reps = newReps
                
                // Refresh listu
                adapter.notifyItemChanged(exPos)
                
                // Přepnutí do módu "Ukládání"
                enableSaveMode()
            }
            .setNegativeButton("Zrušit", null)
            .show()
    }

    /**
     * Změní ikonku Sdílet na Uložit (disketa/fajfka)
     */
    private fun enableSaveMode() {
        if (!isModified) {
            isModified = true
            // Změníme ikonku na "Save"
            btnShare.setImageResource(android.R.drawable.ic_menu_save)
            Toast.makeText(this, "Nezapomeň změny uložit!", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Uloží aktualizovaný workout do Firestore
     */
    private fun saveChangesToFirestore() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        
        if (workout.id.isEmpty()) {
            Toast.makeText(this, "Chyba: Chybí ID tréninku", Toast.LENGTH_SHORT).show()
            return
        }

        btnShare.isEnabled = false // Zabránit double-clicku

        FirebaseFirestore.getInstance().collection("users").document(userId)
            .collection("workouts").document(workout.id)
            .set(workout) // .set() přepíše dokument novými daty
            .addOnSuccessListener {
                Toast.makeText(this, "Trénink aktualizován!", Toast.LENGTH_SHORT).show()
                finish() // Zavřeme aktivitu a vrátíme se do historie
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Chyba ukládání: ${e.message}", Toast.LENGTH_LONG).show()
                btnShare.isEnabled = true
            }
    }

    /**
     * Sdílí trénink jako text přes systémový share dialog
     * Vytvoří formátovaný text s názvem, datem a všemi cviky
     */
    private fun shareWorkout() {
        // === SESTAVENÍ TEXTU ===
        val sdf = SimpleDateFormat("dd. MMM yyyy", Locale.getDefault())
        val dateStr = sdf.format(workout.date)
        val minutes = workout.durationSeconds / 60

        val builder = StringBuilder()
        builder.append("💪 ${workout.name}\n")
        builder.append("📅 $dateStr\n")
        builder.append("⏱️ $minutes min\n")
        builder.append("\n")

        // Přidání cviků a sérií
        workout.exercises.forEachIndexed { exerciseIndex, exercise ->
            builder.append("${exerciseIndex + 1}. ${exercise.name}\n")

            exercise.sets.forEachIndexed { setIndex, set ->
                builder.append("   ${setIndex + 1}×  ${set.weight} kg × ${set.reps} opakování\n")
            }

            builder.append("\n")
        }

        // Přidání statistik na konec
        val totalSets = workout.exercises.sumOf { it.sets.size }
        builder.append("📊 Celkem: ${workout.exercises.size} cviků, $totalSets sérií")

        // === SHARE INTENT ===
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Můj trénink: ${workout.name}")
            putExtra(Intent.EXTRA_TEXT, builder.toString())
        }

        // Zobrazení share dialogu
        startActivity(Intent.createChooser(shareIntent, "Sdílet trénink přes..."))
    }
}