package com.example.fitnesstracker.fragments

import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.fitnesstracker.databinding.DialogAddWeightBinding
import com.example.fitnesstracker.databinding.FragmentWeightBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

class WeightFragment : Fragment() {

    private var _binding: FragmentWeightBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Uložíme si aktuální hodnoty pro výpočty
    private var currentStartWeight = 0f
    private var currentTargetWeight = 0f
    private var currentWeight = 0f

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWeightBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startListeningForData() // Změna názvu funkce

        binding.fabAddWeight.setOnClickListener {
            showAddWeightDialog()
        }
    }

    private fun startListeningForData() {
        val userId = auth.currentUser?.uid ?: return

        // 1. Posloucháme změny v PROFILU (Start & Cíl) - ŽIVĚ
        db.collection("users").document(userId).addSnapshotListener { snapshot, e ->
            if (_binding == null) return@addSnapshotListener
            if (e != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener

            // Kdykoliv se změní profil, tohle se spustí
            currentTargetWeight = snapshot.getDouble("targetWeight")?.toFloat() ?: 0f
            currentStartWeight = snapshot.getDouble("startWeight")?.toFloat() ?: 0f

            // Přepočítáme graf s novými limity (pokud už máme aktuální váhu)
            if (currentWeight > 0) {
                updateProgressBar(currentWeight, currentStartWeight, currentTargetWeight)
            }
        }

        // 2. Posloucháme změny ve VÁHÁCH (Historie) - ŽIVĚ
        db.collection("users").document(userId).collection("weights")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(10)
            .addSnapshotListener { snapshots, e ->
                if (_binding == null) return@addSnapshotListener
                if (e != null || snapshots == null) return@addSnapshotListener

                if (!snapshots.isEmpty) {
                    currentWeight = snapshots.documents[0].getDouble("weight")?.toFloat() ?: 0f
                    binding.tvBigWeight.text = "$currentWeight"

                    // Přepočítáme graf s novou váhou
                    updateProgressBar(currentWeight, currentStartWeight, currentTargetWeight)
                    updateHistoryList(snapshots.documents)
                } else {
                    binding.tvBigWeight.text = "--.-"
                }
            }
    }

    private fun updateProgressBar(current: Float, start: Float, target: Float) {
        if (_binding == null) return

        // Zobrazíme info o cílech
        binding.tvStartInfo.text = "Start: $start kg"

        if (start == 0f || target == 0f) {
            binding.tvTargetInfo.text = "Cíl: $target kg (0 %)"
            binding.circularProgress.progress = 0
            return
        }

        val totalDiff = abs(start - target)
        val currentDiff = abs(start - current)

        // Výpočet procenta (0-100)
        var percentage = ((currentDiff / totalDiff) * 100).toInt()

        if (percentage > 100) percentage = 100
        if (percentage < 0) percentage = 0

        // Logika pro "špatný směr" (přibírám místo hubnutí)
        if (start > target && current > start) percentage = 0
        if (start < target && current < start) percentage = 0

        // Pro animaci násobíme 10x (na škálu 0-1000) pro plynulost
        val progressValue = percentage * 10

        // ANIMACE Progress Baru
        val animation = ObjectAnimator.ofInt(binding.circularProgress, "progress", binding.circularProgress.progress, progressValue)
        animation.duration = 1000
        animation.interpolator = DecelerateInterpolator()
        animation.start()

        binding.tvTargetInfo.text = "Cíl: $target kg ($percentage %)"
    }

    // ... (Zbytek funkcí updateHistoryList, showAddWeightDialog zůstává beze změny) ...

    private fun updateHistoryList(documents: List<com.google.firebase.firestore.DocumentSnapshot>) {
        if (_binding == null) return

        binding.historyContainer.removeAllViews()
        val sdf = SimpleDateFormat("dd. MMM", Locale("cs", "CZ"))

        for (doc in documents) {
            val weight = doc.getDouble("weight")
            val timestamp = doc.getLong("timestamp") ?: 0L
            val dateStr = sdf.format(Date(timestamp))

            val card = androidx.cardview.widget.CardView(requireContext())
            val params = android.widget.LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 0, 24)
            card.layoutParams = params
            card.radius = 30f
            card.cardElevation = 4f
            card.setCardBackgroundColor(Color.WHITE)

            val rowLayout = android.widget.LinearLayout(context)
            rowLayout.orientation = android.widget.LinearLayout.HORIZONTAL
            rowLayout.setPadding(48, 48, 48, 48)
            rowLayout.weightSum = 1f

            val tvDate = TextView(context)
            tvDate.text = dateStr
            tvDate.textSize = 14f
            tvDate.setTextColor(Color.parseColor("#6B7280"))
            val paramsDate = android.widget.LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.5f)
            tvDate.layoutParams = paramsDate

            val tvWeight = TextView(context)
            tvWeight.text = "$weight kg"
            tvWeight.textSize = 18f
            tvWeight.setTypeface(null, android.graphics.Typeface.BOLD)
            tvWeight.setTextColor(Color.parseColor("#4F46E5"))
            tvWeight.gravity = android.view.Gravity.END
            val paramsWeight = android.widget.LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.5f)
            tvWeight.layoutParams = paramsWeight

            rowLayout.addView(tvDate)
            rowLayout.addView(tvWeight)
            card.addView(rowLayout)

            binding.historyContainer.addView(card)
        }
    }

    private fun showAddWeightDialog() {
        val dialogBinding = DialogAddWeightBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnSaveWeight.setOnClickListener {
            val weightStr = dialogBinding.etWeightInput.text.toString()
            val weight = weightStr.toDoubleOrNull()

            if (weight != null && weight > 0) {
                val data = hashMapOf(
                    "weight" to weight,
                    "timestamp" to System.currentTimeMillis()
                )

                db.collection("users").document(auth.uid!!)
                    .collection("weights")
                    .add(data)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Zapsáno", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
            }
        }
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
