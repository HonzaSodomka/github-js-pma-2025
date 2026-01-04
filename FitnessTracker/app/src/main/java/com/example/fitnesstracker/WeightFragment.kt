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
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

/**
 * Fragment pro sledování váhy
 * - Kruhový progress bar ukazující pokrok k cíli
 * - Real-time listeners pro živé aktualizace
 * - Historie posledních 10 záznamů
 * - Validace vstupu (0-500 kg, max 1x denně)
 * - Empty state pokud nemá žádné záznamy
 */
class WeightFragment : Fragment() {

    private var _binding: FragmentWeightBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Uložíme si aktuální hodnoty pro výpočty
    private var currentStartWeight = 0f
    private var currentTargetWeight = 0f
    private var currentWeight = 0f

    // Pro kontrolu zda už dnes přidal váhu
    private var todayTimestamp: Long = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWeightBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Výpočet timestampu pro začátek dnešního dne (00:00:00)
        calculateTodayTimestamp()

        // Spuštění real-time listenerů
        startListeningForData()

        // FAB pro přidání nové váhy
        binding.fabAddWeight.setOnClickListener {
            showAddWeightDialog()
        }
    }

    /**
     * Vypočítá timestamp pro dnešní den (00:00:00)
     * Pro kontrolu zda už dnes přidal váhu
     */
    private fun calculateTodayTimestamp() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        todayTimestamp = calendar.timeInMillis
    }

    /**
     * Nastaví real-time listeners pro:
     * 1. Profil (start váha, cílová váha)
     * 2. Váhy (historie záznamů)
     *
     * Výhoda: Data se aktualizují automaticky při změně
     */
    private fun startListeningForData() {
        val userId = auth.currentUser?.uid ?: return

        // === 1. LISTENER NA PROFIL (Start & Cíl) ===
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

        // === 2. LISTENER NA VÁHY (Historie) ===
        db.collection("users").document(userId).collection("weights")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(10)
            .addSnapshotListener { snapshots, e ->
                if (_binding == null) return@addSnapshotListener
                if (e != null || snapshots == null) return@addSnapshotListener

                if (!snapshots.isEmpty) {
                    // Máme záznamy - zobraz je
                    currentWeight = snapshots.documents[0].getDouble("weight")?.toFloat() ?: 0f
                    binding.tvBigWeight.text = "$currentWeight"

                    // Přepočítáme graf s novou váhou
                    updateProgressBar(currentWeight, currentStartWeight, currentTargetWeight)
                    updateHistoryList(snapshots.documents)

                    // Skrýt empty state
                    showEmptyState(false)
                } else {
                    // Žádné záznamy - zobraz empty state
                    binding.tvBigWeight.text = "--.-"
                    binding.tvStartInfo.text = "Start: --"
                    binding.tvTargetInfo.text = "Cíl: -- (0 %)"
                    binding.circularProgress.progress = 0

                    showEmptyState(true)
                }
            }
    }

    /**
     * Zobrazí/skryje empty state
     */
    private fun showEmptyState(show: Boolean) {
        if (show) {
            binding.historyContainer.removeAllViews()

            // Přidáme prázdný stav přímo do kontejneru
            val emptyText = TextView(requireContext()).apply {
                text = "Zatím žádné záznamy\nPřidej svou první váhu!"
                textSize = 16f
                setTextColor(Color.parseColor("#9CA3AF"))
                gravity = android.view.Gravity.CENTER
                setPadding(0, 48, 0, 48)
            }
            binding.historyContainer.addView(emptyText)
        }
    }

    /**
     * Aktualizuje kruhový progress bar
     * - Výpočet % pokroku k cíli
     * - Animace změny
     */
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

        // === ANIMACE PROGRESS BARU ===
        val animation = ObjectAnimator.ofInt(binding.circularProgress, "progress", binding.circularProgress.progress, progressValue)
        animation.duration = 1000
        animation.interpolator = DecelerateInterpolator()
        animation.start()

        binding.tvTargetInfo.text = "Cíl: $target kg ($percentage %)"
    }

    /**
     * Aktualizuje seznam historie (posledních 10 záznamů)
     */
    private fun updateHistoryList(documents: List<com.google.firebase.firestore.DocumentSnapshot>) {
        if (_binding == null) return

        binding.historyContainer.removeAllViews()
        val sdf = SimpleDateFormat("dd. MMM", Locale("cs", "CZ"))

        for (doc in documents) {
            val weight = doc.getDouble("weight")
            val timestamp = doc.getLong("timestamp") ?: 0L
            val dateStr = sdf.format(Date(timestamp))

            // Vytvoření karty pro jeden záznam
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

            // Datum
            val tvDate = TextView(context)
            tvDate.text = dateStr
            tvDate.textSize = 14f
            tvDate.setTextColor(Color.parseColor("#6B7280"))
            val paramsDate = android.widget.LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.5f)
            tvDate.layoutParams = paramsDate

            // Váha
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

    /**
     * Dialog pro přidání nové váhy
     * - Validace vstupu (0-500 kg)
     * - Kontrola duplicity (max 1x denně)
     */
    private fun showAddWeightDialog() {
        val dialogBinding = DialogAddWeightBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnSaveWeight.setOnClickListener {
            val weightStr = dialogBinding.etWeightInput.text.toString()
            val weight = weightStr.toDoubleOrNull()

            // === VALIDACE PRÁZDNÉHO POLE ===
            if (weight == null) {
                dialogBinding.etWeightInput.error = "Zadej váhu"
                Toast.makeText(context, "Zadej platnou váhu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // === VALIDACE ROZSAHU (0-500 kg) ===
            if (weight <= 0 || weight > 500) {
                dialogBinding.etWeightInput.error = "Rozsah: 0-500 kg"
                Toast.makeText(context, "Váha musí být mezi 0-500 kg", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // === KONTROLA DUPLICITY (MAX 1X DENNĚ) ===
            checkIfAlreadyAddedToday { alreadyAdded ->
                if (alreadyAdded) {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Už jsi dnes přidal váhu")
                        .setMessage("Opravdu chceš přidat další záznam pro dnešek?")
                        .setPositiveButton("Ano, přidat") { _, _ ->
                            saveWeight(weight, dialog)
                        }
                        .setNegativeButton("Zrušit", null)
                        .show()
                } else {
                    // Ještě dnes nepřidal - ulož normálně
                    saveWeight(weight, dialog)
                }
            }
        }

        dialog.show()
    }

    /**
     * Zkontroluje jestli už dnes přidal váhu
     * @param callback Vrátí true pokud už dnes přidal, false pokud ne
     */
    private fun checkIfAlreadyAddedToday(callback: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId).collection("weights")
            .whereGreaterThanOrEqualTo("timestamp", todayTimestamp)
            .get()
            .addOnSuccessListener { result ->
                callback(!result.isEmpty)
            }
            .addOnFailureListener {
                // Při chybě raději povolíme přidání
                callback(false)
            }
    }

    /**
     * Uloží váhu do Firestore
     */
    private fun saveWeight(weight: Double, dialog: AlertDialog) {
        val userId = auth.currentUser?.uid ?: return

        val data = hashMapOf(
            "weight" to weight,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("users").document(userId)
            .collection("weights")
            .add(data)
            .addOnSuccessListener {
                Toast.makeText(context, "Zapsáno", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                // Listener automaticky aktualizuje UI
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Chyba při ukládání: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Cleanup při zničení view - prevence memory leaks
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}