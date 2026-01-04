package com.example.fitnesstracker

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Registrační obrazovka
 * - Vytvoření nového účtu přes Firebase Authentication
 * - Validace emailu, hesla a shody hesel
 * - Vytvoření základního profilu ve Firestore
 * - Automatické přihlášení po registraci
 */
class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Inicializace Firebase služeb
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // UI komponenty
        val etEmail = findViewById<TextInputEditText>(R.id.etRegisterEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etRegisterPassword)
        val etConfirmPassword = findViewById<TextInputEditText>(R.id.etRegisterConfirmPassword)
        val btnRegister = findViewById<MaterialButton>(R.id.btnRegister)
        val tvGoToLogin = findViewById<TextView>(R.id.tvGoToLogin)

        // === REGISTRACE ===
        btnRegister.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            // === VALIDACE PRÁZDNÝCH POLÍ ===
            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Vyplň prosím všechna pole", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // === VALIDACE FORMÁTU EMAILU ===
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.error = "Neplatný formát emailu"
                Toast.makeText(this, "Zadej platnou emailovou adresu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // === VALIDACE DÉLKY HESLA ===
            if (password.length < 6) {
                etPassword.error = "Minimálně 6 znaků"
                Toast.makeText(this, "Heslo musí mít alespoň 6 znaků", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // === SILNĚJŠÍ VALIDACE HESLA (VOLITELNÉ) ===
            // Kontrola alespoň jednoho čísla
            if (!password.matches(Regex(".*[0-9].*"))) {
                etPassword.error = "Musí obsahovat číslo"
                Toast.makeText(this, "Heslo musí obsahovat alespoň jedno číslo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // === VALIDACE SHODY HESEL ===
            if (password != confirmPassword) {
                etConfirmPassword.error = "Hesla se neshodují"
                Toast.makeText(this, "Hesla se neshodují", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // === LOADING STATE ===
            btnRegister.isEnabled = false
            btnRegister.text = "VYTVÁŘÍM ÚČET..."

            // === VYTVOŘENÍ UŽIVATELE VE FIREBASE AUTH ===
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // === ÚSPĚCH - VYTVOŘENÍ PROFILU VE FIRESTORE ===
                        val userId = auth.currentUser?.uid

                        if (userId != null) {
                            // Základní struktura profilu
                            val userMap = hashMapOf(
                                "email" to email,
                                "name" to "",        // Vyplní si později v profilu
                                "age" to 0,
                                "startWeight" to 0.0,
                                "targetWeight" to 0.0,
                                "currentWeight" to 0.0
                            )

                            // Uložení do Firestore
                            db.collection("users").document(userId)
                                .set(userMap)
                                .addOnSuccessListener {
                                    // Vše OK - přesměrování do aplikace
                                    Toast.makeText(this, "Účet úspěšně vytvořen!", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this, MainActivity::class.java))
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    // Firestore selhalo, ale Auth účet je vytvořený
                                    // Pustíme uživatele dál (profil se vytvoří později)
                                    Toast.makeText(this, "Upozornění: ${e.message}", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this, MainActivity::class.java))
                                    finish()
                                }
                        }
                    } else {
                        // === CHYBA - DETAILNÍ ERROR HANDLING ===
                        val errorMessage = when (val exception = task.exception) {
                            // Email už je registrovaný
                            is FirebaseAuthUserCollisionException -> {
                                "Tento email je již zaregistrovaný. Zkus se přihlásit."
                            }
                            // Heslo je příliš slabé (méně než 6 znaků)
                            is FirebaseAuthWeakPasswordException -> {
                                "Heslo je příliš slabé. Použij silnější heslo."
                            }
                            // Síťová chyba
                            is com.google.firebase.FirebaseNetworkException -> {
                                "Problém s připojením k internetu"
                            }
                            // Neplatný email (teoreticky už ošetřeno validací)
                            is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> {
                                "Neplatný formát emailu"
                            }
                            // Jiná chyba
                            else -> {
                                "Chyba registrace: ${exception?.message}"
                            }
                        }

                        // Zobrazení chyby uživateli
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()

                        // === VRÁCENÍ BUTTONU DO PŮVODNÍHO STAVU ===
                        btnRegister.isEnabled = true
                        btnRegister.text = "VYTVOŘIT ÚČET"
                    }
                }
        }

        // === PŘECHOD NA PŘIHLÁŠENÍ ===
        tvGoToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}