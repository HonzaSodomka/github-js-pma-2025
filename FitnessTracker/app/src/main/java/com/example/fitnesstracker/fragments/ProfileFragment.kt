package com.example.fitnesstracker.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.fitnesstracker.activities.LoginActivity
import com.example.fitnesstracker.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

/**
 * Fragment pro správu uživatelského profilu
 * - Zobrazení a editace jména, věku, startovní a cílové váhy
 * - Generování iniciál pro avatar
 * - Odhlášení uživatele
 */
class ProfileFragment : Fragment() {

    // ViewBinding pro type-safe přístup k views (bez findViewById)
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializace Firebase služeb
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Kontrola přihlášení - pokud není user, odhlásit
        val userId = auth.currentUser?.uid
        if (userId == null) {
            logout()
            return
        }

        // Zobrazení emailu (read-only)
        binding.tvEmailDisplay.text = auth.currentUser?.email

        // Načtení dat z Firestore
        loadProfileData(userId)

        // Event listeners
        binding.btnSave.setOnClickListener { saveProfileData(userId) }
        binding.btnLogout.setOnClickListener { logout() }
    }

    /**
     * Načte data profilu z Firestore
     * - Jméno, věk, startovní a cílová váha
     * - Vygeneruje iniciály z jména
     */
    private fun loadProfileData(userId: String) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                // Null-safety check - fragment mohl být už zničen
                if (_binding == null) return@addOnSuccessListener

                if (document.exists()) {
                    // Načtení jména a generování iniciál pro avatar
                    val name = document.getString("name") ?: ""
                    binding.etName.setText(name)
                    generateInitials(name)

                    // Načtení číselných hodnot (zobrazíme jen pokud > 0)
                    val age = document.getLong("age")
                    if (age != null && age > 0) binding.etAge.setText(age.toString())

                    val startW = document.getDouble("startWeight")
                    if (startW != null && startW > 0) binding.etStartWeight.setText(startW.toString())

                    val targetW = document.getDouble("targetWeight")
                    if (targetW != null && targetW > 0) binding.etTargetWeight.setText(targetW.toString())
                }
            }
            .addOnFailureListener { e ->
                // Error handling - zobrazení chyby uživateli
                if (_binding != null) {
                    Toast.makeText(context, "Chyba načítání: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    /**
     * Generuje iniciály z jména pro avatar kruh
     * Např. "Jan Novák" -> "JN"
     */
    private fun generateInitials(name: String) {
        if (name.isBlank()) {
            binding.tvInitials.text = "?"
            return
        }

        val parts = name.trim().split(" ")
        var initials = ""

        if (parts.isNotEmpty()) {
            // První písmeno prvního slova
            initials += parts[0].take(1).uppercase(Locale.getDefault())

            // První písmeno druhého slova (pokud existuje)
            if (parts.size > 1) {
                initials += parts[1].take(1).uppercase(Locale.getDefault())
            }
        }

        binding.tvInitials.text = initials
    }

    /**
     * Uloží změny profilu do Firestore
     * - Validace všech vstupů
     * - Loading state během ukládání
     * - Update nebo Set (fallback pokud dokument neexistuje)
     */
    private fun saveProfileData(userId: String) {
        // Získání hodnot z formuláře
        val newName = binding.etName.text.toString().trim()
        val ageStr = binding.etAge.text.toString().trim()
        val startWStr = binding.etStartWeight.text.toString().trim()
        val targetWStr = binding.etTargetWeight.text.toString().trim()

        // === VALIDACE JMÉNA ===
        if (newName.isEmpty()) {
            binding.etName.error = "Jméno nemůže být prázdné"
            return
        }

        // Konverze stringů na čísla (default 0 pokud špatný formát)
        val age = ageStr.toIntOrNull() ?: 0
        val startW = startWStr.toDoubleOrNull() ?: 0.0
        val targetW = targetWStr.toDoubleOrNull() ?: 0.0

        // === VALIDACE VĚKU ===
        if (age < 0 || age > 150) {
            binding.etAge.error = "Neplatný věk"
            Toast.makeText(context, "Zadej věk mezi 0-150", Toast.LENGTH_SHORT).show()
            return
        }

        // === VALIDACE VÁHY ===
        if (startW < 0 || startW > 500) {
            binding.etStartWeight.error = "Neplatná váha"
            return
        }

        if (targetW < 0 || targetW > 500) {
            binding.etTargetWeight.error = "Neplatná váha"
            return
        }

        // === LOADING STATE ===
        // Disable tlačítka během ukládání (prevence double-click)
        binding.btnSave.isEnabled = false
        binding.btnSave.text = "UKLÁDÁM..."

        // Data pro uložení
        val updates = hashMapOf<String, Any>(
            "name" to newName,
            "age" to age,
            "startWeight" to startW,
            "targetWeight" to targetW
        )

        // === ULOŽENÍ DO FIRESTORE ===
        db.collection("users").document(userId)
            .update(updates)
            .addOnSuccessListener {
                if (_binding != null) {
                    Toast.makeText(context, "✅ Uloženo!", Toast.LENGTH_SHORT).show()
                    generateInitials(newName) // Aktualizace iniciál

                    // Vrácení tlačítka do původního stavu
                    binding.btnSave.isEnabled = true
                    binding.btnSave.text = "ULOŽIT ZMĚNY"
                }
            }
            .addOnFailureListener {
                // Fallback: Pokud dokument neexistuje, vytvoříme ho (.set místo .update)
                db.collection("users").document(userId).set(updates)
                    .addOnSuccessListener {
                        if (_binding != null) {
                            Toast.makeText(context, "✅ Uloženo!", Toast.LENGTH_SHORT).show()
                            generateInitials(newName)

                            binding.btnSave.isEnabled = true
                            binding.btnSave.text = "ULOŽIT ZMĚNY"
                        }
                    }
                    .addOnFailureListener { e ->
                        // Totální selhání - zobrazit chybu
                        if (_binding != null) {
                            Toast.makeText(context, "Chyba při ukládání: ${e.message}", Toast.LENGTH_SHORT).show()

                            binding.btnSave.isEnabled = true
                            binding.btnSave.text = "ULOŽIT ZMĚNY"
                        }
                    }
            }
    }

    /**
     * Odhlášení uživatele
     * - SignOut z Firebase Auth
     * - Redirect na LoginActivity s vyčištěním back stacku
     */
    private fun logout() {
        auth.signOut()
        val intent = Intent(requireActivity(), LoginActivity::class.java)
        // Vyčistí celý back stack, aby se user nemohl vrátit zpět
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    /**
     * Cleanup při zničení view - prevence memory leaks
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}