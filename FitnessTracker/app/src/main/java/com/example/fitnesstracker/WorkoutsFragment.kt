package com.example.fitnesstracker.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnesstracker.ActiveWorkoutActivity
import com.example.fitnesstracker.WorkoutDetailActivity // Tuto aktivitu vytvoříme za chvíli
import com.example.fitnesstracker.R
import com.example.fitnesstracker.adapters.WorkoutHistoryAdapter
import com.example.fitnesstracker.models.Workout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class WorkoutsFragment : Fragment() {

    private lateinit var rvHistory: RecyclerView
    private lateinit var btnStart: Button
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

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        rvHistory = view.findViewById(R.id.rvWorkoutHistory)
        btnStart = view.findViewById(R.id.btnStartWorkout)

        rvHistory.layoutManager = LinearLayoutManager(context)

        btnStart.setOnClickListener {
            val intent = Intent(requireContext(), ActiveWorkoutActivity::class.java)
            startActivity(intent)
        }

        loadHistory()
    }

    override fun onResume() {
        super.onResume()
        loadHistory()
    }

    private fun loadHistory() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId).collection("workouts")
            .orderBy("date", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val workoutList = ArrayList<Workout>()

                for (document in result) {
                    // DŮLEŽITÉ: Převedeme na objekt a RUČNĚ nastavíme ID dokumentu
                    val workout = document.toObject(Workout::class.java)
                    workout.id = document.id // Abychom věděli, co mazat
                    workoutList.add(workout)
                }

                // Nastavíme adaptér s funkcemi pro klik a long-click
                rvHistory.adapter = WorkoutHistoryAdapter(
                    workoutList,
                    onItemClick = { workout -> openDetail(workout) },
                    onItemLongClick = { workout -> showDeleteDialog(workout) }
                )
            }
            .addOnFailureListener {
                Toast.makeText(context, "Chyba načítání: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun openDetail(workout: Workout) {
        val intent = Intent(requireContext(), WorkoutDetailActivity::class.java)
        intent.putExtra("WORKOUT_DATA", workout) // Posíláme celý objekt (Parcelable)
        startActivity(intent)
    }

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

    private fun deleteWorkout(workout: Workout) {
        val userId = auth.currentUser?.uid ?: return

        // Pokud ID chybí (stará data), nemůžeme mazat
        if (workout.id.isEmpty()) return

        db.collection("users").document(userId).collection("workouts")
            .document(workout.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Smazáno", Toast.LENGTH_SHORT).show()
                loadHistory() // Obnovit seznam
            }
            .addOnFailureListener {
                Toast.makeText(context, "Chyba při mazání", Toast.LENGTH_SHORT).show()
            }
    }
}
