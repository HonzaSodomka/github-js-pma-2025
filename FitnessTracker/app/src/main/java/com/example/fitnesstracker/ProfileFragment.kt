package com.example.fitnesstracker.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.fitnesstracker.LoginActivity
import com.example.fitnesstracker.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class ProfileFragment : Fragment() {

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

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val userId = auth.currentUser?.uid
        if (userId == null) {
            logout()
            return
        }

        binding.tvEmailDisplay.text = auth.currentUser?.email
        loadProfileData(userId)

        binding.btnSave.setOnClickListener { saveProfileData(userId) }
        binding.btnLogout.setOnClickListener { logout() }
    }

    private fun loadProfileData(userId: String) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (_binding == null) return@addOnSuccessListener

                if (document.exists()) {
                    val name = document.getString("name") ?: ""
                    binding.etName.setText(name) // Tady se to vloží do edit textu
                    generateInitials(name)

                    val age = document.getLong("age")
                    if (age != null && age > 0) binding.etAge.setText(age.toString())

                    val startW = document.getDouble("startWeight")
                    if (startW != null && startW > 0) binding.etStartWeight.setText(startW.toString())

                    val targetW = document.getDouble("targetWeight")
                    if (targetW != null && targetW > 0) binding.etTargetWeight.setText(targetW.toString())
                }
            }
    }

    private fun generateInitials(name: String) {
        if (name.isBlank()) {
            binding.tvInitials.text = "?"
            return
        }
        val parts = name.trim().split(" ")
        var initials = ""
        if (parts.isNotEmpty()) {
            initials += parts[0].take(1).uppercase(Locale.getDefault())
            if (parts.size > 1) {
                initials += parts[1].take(1).uppercase(Locale.getDefault())
            }
        }
        binding.tvInitials.text = initials
    }

    private fun saveProfileData(userId: String) {
        // Bereme jméno přímo z edit textu
        val newName = binding.etName.text.toString().trim()
        val ageStr = binding.etAge.text.toString().trim()
        val startWStr = binding.etStartWeight.text.toString().trim()
        val targetWStr = binding.etTargetWeight.text.toString().trim()

        val age = ageStr.toIntOrNull() ?: 0
        val startW = startWStr.toDoubleOrNull() ?: 0.0
        val targetW = targetWStr.toDoubleOrNull() ?: 0.0

        val updates = hashMapOf<String, Any>(
            "name" to newName,
            "age" to age,
            "startWeight" to startW,
            "targetWeight" to targetW
        )

        db.collection("users").document(userId)
            .update(updates)
            .addOnSuccessListener {
                if (_binding != null) {
                    Toast.makeText(context, "✅ Uloženo!", Toast.LENGTH_SHORT).show()
                    // Zkusíme aktualizovat iniciály hned po uložení
                    generateInitials(newName)
                }
            }
            .addOnFailureListener {
                db.collection("users").document(userId).set(updates)
            }
    }

    private fun logout() {
        auth.signOut()
        val intent = Intent(requireActivity(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
