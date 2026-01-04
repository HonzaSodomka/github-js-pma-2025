package com.example.fitnesstracker.activities

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
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
import com.example.fitnesstracker.adapters.ActiveWorkoutAdapter
import com.example.fitnesstracker.models.Workout
import com.example.fitnesstracker.models.WorkoutExercise
import com.example.fitnesstracker.utils.CustomExercise
import com.example.fitnesstracker.utils.ExerciseData
import com.google.android.material.appbar.AppBarLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

/**
 * Aktivita pro záznam aktivního tréninku
 * - Přidávání cviků a sérií v reálném čase
 * - Timer měřící délku tréninku
 * - Auto-naming podle kategorií cviků
 * - Uložení do Firestore po dokončení
 */
class ActiveWorkoutActivity : AppCompatActivity() {

    private lateinit var rvExercises: RecyclerView
    private lateinit var adapter: ActiveWorkoutAdapter
    private lateinit var btnAddExercise: Button
    private lateinit var btnFinish: Button
    private lateinit var btnClose: TextView
    private lateinit var etWorkoutName: EditText
    private lateinit var tvTimer: TextView
    private lateinit var appBarLayout: AppBarLayout

    private val currentWorkout = Workout()
    private var startTime: Long = 0
    private val customExercisesList = ArrayList<CustomExercise>()

    // Klíč pro uložení stavu při rotaci
    companion object {
        private const val KEY_WORKOUT = "workout"
        private const val KEY_START_TIME = "start_time"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_active_workout)

        appBarLayout = findViewById(R.id.appBarLayout)

        // Edge-to-edge handling pro notch/výřez displeje
        ViewCompat.setOnApplyWindowInsetsListener(appBarLayout) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        // Setup UI
        rvExercises = findViewById(R.id.rvExercises)
        btnAddExercise = findViewById(R.id.btnAddExercise)
        btnFinish = findViewById(R.id.btnFinish)
        btnClose = findViewById(R.id.btnClose)
        etWorkoutName = findViewById(R.id.etWorkoutName)
        tvTimer = findViewById(R.id.tvTimer)

        // === OBNOVA STAVU PŘI ROTACI ===
        if (savedInstanceState != null) {
            // Obnovíme workout data a čas startu
            val savedWorkout = savedInstanceState.getParcelable<Workout>(KEY_WORKOUT)
            if (savedWorkout != null) {
                currentWorkout.exercises.clear()
                currentWorkout.exercises.addAll(savedWorkout.exercises)
                currentWorkout.name = savedWorkout.name
                etWorkoutName.setText(savedWorkout.name)
            }
            startTime = savedInstanceState.getLong(KEY_START_TIME, System.currentTimeMillis())
        } else {
            // Nový trénink - nastav aktuální čas
            startTime = System.currentTimeMillis()
        }

        // Spuštění timeru
        startTimerDisplay()

        // Načtení vlastních cviků z Firestore
        loadCustomExercises()

        // Setup RecyclerView s adapterem
        rvExercises.layoutManager = LinearLayoutManager(this)
        adapter = ActiveWorkoutAdapter(currentWorkout.exercises) { position ->
            deleteExercise(position)
        }
        rvExercises.adapter = adapter

        // Event listeners
        btnAddExercise.setOnClickListener {
            showCategorySelectionDialog()
        }

        btnFinish.setOnClickListener {
            showFinishConfirmationDialog() // Nový confirmation dialog
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

    /**
     * Uložení stavu při rotaci obrazovky
     * Bez tohoto by se při otočení mobilu ztratil celý trénink
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // Aktualizujeme název tréninku před uložením
        currentWorkout.name = etWorkoutName.text.toString()

        // Uložíme workout a čas startu
        outState.putParcelable(KEY_WORKOUT, currentWorkout)
        outState.putLong(KEY_START_TIME, startTime)
    }

    /**
     * Načte vlastní cviky uživatele z Firestore
     * Tyto cviky se pak přidají k výchozím cvikům při výběru
     */
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
            .addOnFailureListener { e ->
                // Chyba načítání - nevadí, budeme jen bez vlastních cviků
                Toast.makeText(this, "Nepodařilo se načíst vlastní cviky: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Dialog pro výběr kategorie (Hrudník, Záda, Nohy...)
     */
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

    /**
     * Dialog pro výběr konkrétního cviku v kategorii
     * Kombinuje výchozí + vlastní cviky uživatele
     */
    private fun showExerciseSelectionDialog(category: String) {
        // Výchozí cviky z dané kategorie
        val exercises = ExerciseData.categories[category]?.toMutableList() ?: mutableListOf()

        // Přidání vlastních cviků ze stejné kategorie
        val myCustom = customExercisesList.filter { it.category == category }
        for (c in myCustom) exercises.add(c.name)

        // Seřazení abecedně
        exercises.sort()

        // Přidání možnosti vytvořit nový cvik na začátek
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

    /**
     * Dialog pro vytvoření nového vlastního cviku během tréninku
     */
    private fun showCreateCustomExerciseDialog(category: String) {
        val input = EditText(this)
        input.hint = "Název cviku"
        val container = FrameLayout(this)
        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
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

    /**
     * Uloží nový vlastní cvik do Firestore
     * Po uložení ho automaticky přidá do tréninku
     */
    private fun saveCustomExerciseToFirestore(name: String, category: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("users").document(userId)
            .collection("custom_exercises").add(hashMapOf("name" to name, "category" to category))
            .addOnSuccessListener {
                Toast.makeText(this, "Cvik uložen", Toast.LENGTH_SHORT).show()
                customExercisesList.add(CustomExercise(name, category))
                addNewExercise(name)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Chyba při ukládání cviku: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Přidá nový cvik do tréninku
     * Scrollne na něj pro lepší UX
     */
    private fun addNewExercise(name: String) {
        currentWorkout.exercises.add(WorkoutExercise(name = name))
        adapter.notifyItemInserted(currentWorkout.exercises.size - 1)
        rvExercises.scrollToPosition(currentWorkout.exercises.size - 1)
    }

    /**
     * Smaže cvik z tréninku
     */
    private fun deleteExercise(position: Int) {
        currentWorkout.exercises.removeAt(position)
        adapter.notifyItemRemoved(position)
    }

    /**
     * === NOVÝ === Confirmation dialog před dokončením tréninku
     * Zobrazí souhrn tréninku
     */
    private fun showFinishConfirmationDialog() {
        // Základní validace - prázdný trénink
        if (currentWorkout.exercises.isEmpty()) {
            Toast.makeText(this, "Trénink je prázdný", Toast.LENGTH_SHORT).show()
            return
        }

        // === VALIDACE ČE KAŽDÝ CVIK MÁ ALESPOŇ 1 SÉRII ===
        val emptyCviky = currentWorkout.exercises.filter { it.sets.isEmpty() }
        if (emptyCviky.isNotEmpty()) {
            val names = emptyCviky.joinToString(", ") { it.name }
            AlertDialog.Builder(this)
                .setTitle("Prázdné cviky")
                .setMessage("Tyto cviky nemají žádné série:\n\n$names\n\nChceš je odstranit a pokračovat?")
                .setPositiveButton("Odstranit a dokončit") { _, _ ->
                    // Odstraníme cviky bez sérií
                    currentWorkout.exercises.removeAll(emptyCviky)

                    // Zkontrolujeme znovu jestli ještě něco zbývá
                    if (currentWorkout.exercises.isEmpty()) {
                        Toast.makeText(this, "Všechny cviky byly prázdné", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    // Pokračujeme s dokončením
                    showFinishConfirmationDialog()
                }
                .setNegativeButton("Zpět", null)
                .show()
            return
        }

        // Spočítání celkového počtu sérií
        val totalSets = currentWorkout.exercises.sumOf { it.sets.size }
        val duration = (System.currentTimeMillis() - startTime) / 1000 / 60 // minuty

        // Confirmation dialog s informacemi
        AlertDialog.Builder(this)
            .setTitle("Dokončit trénink?")
            .setMessage("""
                Cviky: ${currentWorkout.exercises.size}
                Série: $totalSets
                Čas: $duration min
                
                Uložit tento trénink?
            """.trimIndent())
            .setPositiveButton("Dokončit") { _, _ ->
                finishWorkout()
            }
            .setNegativeButton("Pokračovat v tréninku", null)
            .show()
    }

    /**
     * Dokončí a uloží trénink do Firestore
     * - Auto-naming pokud uživatel nezadal vlastní název
     * - Výpočet délky tréninku
     */
    private fun finishWorkout() {
        // Auto-naming podle kategorií cviků
        val currentName = etWorkoutName.text.toString()
        if (currentName == "Dnešní trénink" || currentName.isEmpty()) {
            // Získáme kategorie všech cviků
            val cats = mutableSetOf<String>()
            for (ex in currentWorkout.exercises) {
                val c = ExerciseData.getCategoryForExercise(ex.name, customExercisesList)
                if (c != "Ostatní") cats.add(c)
            }
            // Název podle kategorií (např. "Hrudník & Triceps" nebo "Full Body")
            currentWorkout.name = if (cats.isNotEmpty()) cats.joinToString(" & ") else "Full Body"
        } else {
            currentWorkout.name = currentName
        }

        // Nastavení datumu a délky
        currentWorkout.date = Date()
        currentWorkout.durationSeconds = (System.currentTimeMillis() - startTime) / 1000

        // Loading state
        btnFinish.isEnabled = false
        btnFinish.text = "UKLÁDÁM..."

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("users").document(userId)
            .collection("workouts").add(currentWorkout)
            .addOnSuccessListener {
                Toast.makeText(this, "Uloženo!", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener { e ->
                // Error handling - vrátit button do původního stavu
                btnFinish.isEnabled = true
                btnFinish.text = "DOKONČIT"
                Toast.makeText(this, "Chyba při ukládání: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Timer zobrazující délku tréninku
     * Běží každou sekundu v main thread pomocí Handler
     */
    private fun startTimerDisplay() {
        val handler = Handler(Looper.getMainLooper())
        handler.post(object : Runnable {
            override fun run() {
                val millis = System.currentTimeMillis() - startTime
                val sec = (millis / 1000) % 60
                val min = (millis / (1000 * 60)) % 60
                val hr = (millis / (1000 * 60 * 60))

                // Formát: "1:23:45" nebo "23:45"
                tvTimer.text = if (hr > 0) String.format("%d:%02d:%02d", hr, min, sec)
                else String.format("%02d:%02d", min, sec)

                // Další update za 1 sekundu
                handler.postDelayed(this, 1000)
            }
        })
    }
}