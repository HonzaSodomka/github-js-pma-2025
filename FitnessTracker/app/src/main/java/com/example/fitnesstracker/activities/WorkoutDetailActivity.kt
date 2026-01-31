package com.example.fitnesstracker.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
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
 * - Obsahuje tlačítko UPRAVIT CVIKY, které aktivuje editační mód
 */
class WorkoutDetailActivity : AppCompatActivity() {

    private lateinit var workout: Workout
    private lateinit var adapter: WorkoutDetailAdapter
    private lateinit var btnShare: ImageButton
    private lateinit var btnEditMode: Button // Naše nové textové tlačítko
    
    private var isModified = false 
    private var isEditMode = false // Sleduje, jestli jsme v režimu úprav

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

        // === 1. PŘIDÁNÍ TEXTOVÉHO TLAČÍTKA "UPRAVIT" DO HLAVIČKY ===
        // Vytvoříme tlačítko programově a vložíme ho do AppBarLayoutu
        btnEditMode = Button(this).apply {
            text = "UPRAVIT CVIKY"
            textSize = 14f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#4F46E5")) // Indigo barva
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(40, 0, 40, 20)
            }
        }
        // Vložíme ho na konec AppBarLayoutu (pod statistiky)
        appBar.addView(btnEditMode)

        // Vyplnění dat
        tvDetailName.text = workout.name
        val sdf = SimpleDateFormat("dd. MMM yyyy", Locale.getDefault())
        tvDetailDate.text = sdf.format(workout.date).uppercase()
        val minutes = workout.durationSeconds / 60
        tvDetailDuration.text = "$minutes MIN"

        // === SETUP ADAPTERU ===
        rvExercises.layoutManager = LinearLayoutManager(this)
        adapter = WorkoutDetailAdapter(workout.exercises) { exPos, setPos, set ->
            // Callback po kliknutí na sérii (funguje jen v edit módu)
            if (isEditMode) {
                showEditSetDialog(exPos, setPos, set)
            } else {
                Toast.makeText(this, "Klikni na 'UPRAVIT CVIKY' pro změnu", Toast.LENGTH_SHORT).show()
            }
        }
        rvExercises.adapter = adapter

        // === LISTENERS ===
        btnBack.setOnClickListener { finish() }
        
        btnShare.setOnClickListener { 
            if (isModified) saveChangesToFirestore() else shareWorkout() 
        }

        // Logika přepínání Edit módu
        btnEditMode.setOnClickListener {
            toggleEditMode()
        }
    }

    private fun toggleEditMode() {
        isEditMode = !isEditMode
        
        if (isEditMode) {
            btnEditMode.text = "UKONČIT ÚPRAVY"
            btnEditMode.setBackgroundColor(Color.parseColor("#EF4444")) // Červená pro ukončení
            Toast.makeText(this, "Nyní můžeš kliknout na série a upravit je", Toast.LENGTH_LONG).show()
        } else {
            btnEditMode.text = "UPRAVIT CVIKY"
            btnEditMode.setBackgroundColor(Color.parseColor("#4F46E5")) // Zpět na modrou
        }

        // Řekneme adaptéru, že se změnil mód (překreslí ikonky tužky)
        adapter.setEditMode(isEditMode)
    }

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

                // Aplikování změny
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
            btnShare.setImageResource(android.R.drawable.ic_menu_save) // Ikonka diskety
            Toast.makeText(this, "Změny provedeny. Klikni vpravo nahoře pro ULOŽENÍ!", Toast.LENGTH_LONG).show()
        }
    }

    private fun saveChangesToFirestore() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        
        if (workout.id.isEmpty()) return

        btnShare.isEnabled = false
        btnEditMode.isEnabled = false // Zablokovat i edit button

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