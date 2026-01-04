package com.example.fitnesstracker.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnesstracker.R
import com.example.fitnesstracker.adapters.*
import com.example.fitnesstracker.models.Workout
import com.google.android.material.appbar.AppBarLayout
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Aktivita zobrazující detail dokončeného tréninku
 * - Název, datum, délka tréninku
 * - Seznam cviků s jejich sériemi (RecyclerView)
 * - Možnost sdílení tréninku jako text
 * - Edge-to-edge design
 */
class WorkoutDetailActivity : AppCompatActivity() {

    private lateinit var workout: Workout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_workout_detail)

        // === EDGE-TO-EDGE HANDLING ===
        val appBar = findViewById<AppBarLayout>(R.id.appBarLayout)

        ViewCompat.setOnApplyWindowInsetsListener(appBar) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        // === NAČTENÍ DAT Z INTENTU ===
        workout = intent.getParcelableExtra<Workout>("WORKOUT_DATA") ?: run {
            finish()
            return
        }

        // === UI KOMPONENTY ===
        val tvDetailName = findViewById<TextView>(R.id.tvDetailName)
        val tvDetailDate = findViewById<TextView>(R.id.tvDetailDate)
        val tvDetailDuration = findViewById<TextView>(R.id.tvDetailDuration)
        val rvExercises = findViewById<RecyclerView>(R.id.rvExercises)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnShare = findViewById<ImageButton>(R.id.btnShare)

        // === VYPLNĚNÍ DAT ===
        tvDetailName.text = workout.name

        val sdf = SimpleDateFormat("dd. MMM yyyy", Locale.getDefault())
        tvDetailDate.text = sdf.format(workout.date).uppercase()

        val minutes = workout.durationSeconds / 60
        tvDetailDuration.text = "$minutes MIN"

        // === SETUP RECYCLERVIEW ===
        rvExercises.layoutManager = LinearLayoutManager(this)
        rvExercises.adapter = WorkoutDetailAdapter(workout.exercises)

        // === BUTTON LISTENERS ===
        btnBack.setOnClickListener { finish() }
        btnShare.setOnClickListener { shareWorkout() }
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