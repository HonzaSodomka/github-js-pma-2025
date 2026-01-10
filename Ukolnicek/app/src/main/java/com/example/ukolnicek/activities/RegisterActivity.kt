package com.example.ukolnicek.activities

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ukolnicek.MainActivity
import com.example.ukolnicek.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.etEmail.error = "Neplatný email"
                return@setOnClickListener
            }

            if (password.length < 6) {
                binding.etPassword.error = "Heslo musí mít min. 6 znaků"
                return@setOnClickListener
            }

            binding.btnRegister.isEnabled = false
            binding.btnRegister.text = "REGISTRUJI..."

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    // Vytvoříme prázdný dokument uživatele ve Firestore (pro jistotu)
                    val userMap = hashMapOf(
                        "email" to email,
                        "createdAt" to System.currentTimeMillis()
                    )
                    
                    FirebaseFirestore.getInstance().collection("users")
                        .document(result.user!!.uid)
                        .set(userMap)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Registrace úspěšná", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finishAffinity() // Zavře všechny předchozí aktivity
                        }
                }
                .addOnFailureListener { e ->
                    binding.btnRegister.isEnabled = true
                    binding.btnRegister.text = "ZAREGISTROVAT SE"
                    Toast.makeText(this, "Chyba: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        binding.tvLogin.setOnClickListener {
            finish() // Zpět na login
        }
    }
}
