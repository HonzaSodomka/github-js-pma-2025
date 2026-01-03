package com.example.fitnesstracker // Změň na svůj package name!

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val etEmail = findViewById<TextInputEditText>(R.id.etRegisterEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etRegisterPassword)
        val etConfirmPassword = findViewById<TextInputEditText>(R.id.etRegisterConfirmPassword)
        val btnRegister = findViewById<MaterialButton>(R.id.btnRegister)
        val tvGoToLogin = findViewById<TextView>(R.id.tvGoToLogin)

        btnRegister.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            // 1. Validace vstupů
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vyplň prosím všechna pole", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Heslo musí mít alespoň 6 znaků", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                etConfirmPassword.error = "Hesla se neshodují"
                return@setOnClickListener
            }

            btnRegister.isEnabled = false
            btnRegister.text = "VYTVÁŘÍM ÚČET..."

            // 2. Vytvoření uživatele ve Firebase Auth
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid

                        // 3. Vytvoření základního profilu ve Firestore
                        if (userId != null) {
                            val userMap = hashMapOf(
                                "email" to email,
                                "name" to "",        // Zatím prázdné
                                "age" to 0,          // Default
                                "startWeight" to 0.0,
                                "targetWeight" to 0.0,
                                "currentWeight" to 0.0
                            )

                            db.collection("users").document(userId)
                                .set(userMap)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Účet úspěšně vytvořen!", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this, MainActivity::class.java))
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    // Účet je vytvořen, ale DB selhala (vzácné), pustíme ho dál
                                    Toast.makeText(this, "Chyba při ukládání dat: ${e.message}", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this, MainActivity::class.java))
                                    finish()
                                }
                        }
                    } else {
                        btnRegister.isEnabled = true
                        btnRegister.text = "VYTVOŘIT ÚČET"
                        Toast.makeText(this, "Chyba registrace: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        tvGoToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
