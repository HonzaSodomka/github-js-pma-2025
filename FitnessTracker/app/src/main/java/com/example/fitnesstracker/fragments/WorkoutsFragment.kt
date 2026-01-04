package com.example.fitnesstracker.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnesstracker.activities.ActiveWorkoutActivity
import com.example.fitnesstracker.R
import com.example.fitnesstracker.activities.WorkoutDetailActivity
import com.example.fitnesstracker.adapters.WorkoutHistoryAdapter
import com.example.fitnesstracker.models.Workout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

/**
 * Fragment zobrazující historii tréninků
 * - RecyclerView s kartami tréninků (datum, název, stats)
 * - Kliknutí = Detail tréninku
 * - Long-press = Smazání tréninku
 * - Empty state když nemá žádné tréninky
 * - Loading indicator při načítání z Firestore
 */
class WorkoutsFragment : Fragment() {

    private lateinit var rvHistory: RecyclerView
    private lateinit var btnStart: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var llEmptyState: LinearLayout

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_workouts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializace Firebase
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // UI komponenty
        rvHistory = view.findViewById(R.id.rvWorkoutHistory)
        btnStart = view.findViewById(R.id.btnStartWorkout)
        progressBar = view.findViewById(R.id.progressBar)
        llEmptyState = view.findViewById(R.id.llEmptyState)

        // Setup RecyclerView
        rvHistory.layoutManager = LinearLayoutManager(context)

        // Start workout button
        btnStart.setOnClickListener {
            val intent = Intent(requireContext(), ActiveWorkoutActivity::class.java)
            startActivity(intent)
        }

        // Načtení dat
        loadHistory()
    }

    /**
     * Při návratu do fragmentu (např. po dokončení tréninku)
     * znovu načteme data
     */
    override fun onResume() {
        super.onResume()
        loadHistory()
    }

    /**
     * Načte historii tréninků z Firestore
     * - Zobrazuje loading state
     * - Error handling
     * - Empty state pokud není žádný trénink
     */
    private fun loadHistory() {
        val userId = auth.currentUser?.uid

        if (userId == null) {
            Toast.makeText(context, "Nejsi přihlášený", Toast.LENGTH_SHORT).show()
            return
        }

        // === LOADING STATE START ===
        showLoading()

        // === NAČTENÍ Z FIRESTORE ===
        db.collection("users").document(userId).collection("workouts")
            .orderBy("date", Query.Direction.DESCENDING) // Nejnovější první
            .get()
            .addOnSuccessListener { result ->
                val workoutList = ArrayList<Workout>()

                // Převod dokumentů na Workout objekty
                for (document in result) {
                    val workout = document.toObject(Workout::class.java)
                    workout.id = document.id // Důležité pro mazání
                    workoutList.add(workout)
                }

                // === EMPTY STATE CHECK ===
                if (workoutList.isEmpty()) {
                    // Žádné tréninky - zobraz prázdný stav
                    showEmptyState()
                } else {
                    // Máme tréninky - zobraz je
                    // Setup adapteru s callback funkcemi
                    rvHistory.adapter = WorkoutHistoryAdapter(
                        workoutList,
                        onItemClick = { workout -> openDetail(workout) },      // Normální klik
                        onItemLongClick = { workout -> showDeleteDialog(workout) }  // Dlouhý klik
                    )

                    showContent()
                }
            }
            .addOnFailureListener { e ->
                // === ERROR HANDLING ===
                // Rozlišení typů chyb
                val errorMessage = when (e) {
                    is com.google.firebase.FirebaseNetworkException -> {
                        "Problém s připojením k internetu"
                    }
                    is com.google.firebase.firestore.FirebaseFirestoreException -> {
                        "Chyba databáze: ${e.message}"
                    }
                    else -> {
                        "Chyba načítání: ${e.message}"
                    }
                }

                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()

                // I při chybě zobraz empty state (lepší než bílá obrazovka)
                showEmptyState()
            }
    }

    /**
     * Zobrazí loading indicator
     * Skryje RecyclerView i Empty State
     */
    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        rvHistory.visibility = View.GONE
        llEmptyState.visibility = View.GONE
    }

    /**
     * Zobrazí empty state
     * Skryje loading i RecyclerView
     */
    private fun showEmptyState() {
        progressBar.visibility = View.GONE
        rvHistory.visibility = View.GONE
        llEmptyState.visibility = View.VISIBLE
    }

    /**
     * Zobrazí RecyclerView s daty
     * Skryje loading i Empty State
     */
    private fun showContent() {
        progressBar.visibility = View.GONE
        rvHistory.visibility = View.VISIBLE
        llEmptyState.visibility = View.GONE
    }

    /**
     * Otevře detail tréninku
     * Předá celý Workout objekt přes Intent (Parcelable)
     */
    private fun openDetail(workout: Workout) {
        val intent = Intent(requireContext(), WorkoutDetailActivity::class.java)
        intent.putExtra("WORKOUT_DATA", workout)
        startActivity(intent)
    }

    /**
     * Dialog pro potvrzení smazání tréninku
     */
    private fun showDeleteDialog(workout: Workout) {
        AlertDialog.Builder(requireContext())
            .setTitle("Smazat trénink?")
            .setMessage("Opravdu chceš smazat '${workout.name}'? Tato akce je nevratná.")
            .setPositiveButton("Smazat") { _, _ ->
                deleteWorkout(workout)
            }
            .setNegativeButton("Zrušit", null)
            .show()
    }

    /**
     * Smaže trénink z Firestore
     * Po úspěšném smazání reloadne seznam
     */
    private fun deleteWorkout(workout: Workout) {
        val userId = auth.currentUser?.uid ?: return

        // Kontrola že máme ID dokumentu
        if (workout.id.isEmpty()) {
            Toast.makeText(context, "Nelze smazat (chybí ID)", Toast.LENGTH_SHORT).show()
            return
        }

        // Mazání z Firestore
        db.collection("users").document(userId).collection("workouts")
            .document(workout.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Smazáno", Toast.LENGTH_SHORT).show()
                loadHistory() // Reload seznamu
            }
            .addOnFailureListener { e ->
                // Error handling pro mazání
                Toast.makeText(context, "Chyba při mazání: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}