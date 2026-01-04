package com.example.fitnesstracker.activities

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fitnesstracker.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

/**
 * Přihlašovací obrazovka
 * - Firebase Authentication
 * - Auto-redirect pokud už je user přihlášený
 * - Validace emailu a hesla
 * - Detailní error handling
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        // === AUTO-REDIRECT ===
        // Pokud už je uživatel přihlášený, jdi rovnou do aplikace
        if (auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        // UI komponenty
        val etEmail = findViewById<TextInputEditText>(R.id.etLoginEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etLoginPassword)
        val btnLogin = findViewById<MaterialButton>(R.id.btnLogin)
        val tvGoToRegister = findViewById<TextView>(R.id.tvGoToRegister)

        // === PŘIHLÁŠENÍ ===
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // === VALIDACE PRÁZDNÝCH POLÍ ===
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vyplň prosím email i heslo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // === VALIDACE FORMÁTU EMAILU ===
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.error = "Neplatný formát emailu"
                Toast.makeText(this, "Zadej platnou emailovou adresu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // === LOADING STATE ===
            // Disable tlačítka aby uživatel neklikal vícekrát
            btnLogin.isEnabled = false
            btnLogin.text = "PŘIHLAŠUJI..."

            // === FIREBASE AUTHENTICATION ===
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // === ÚSPĚCH - PŘESMĚROVÁNÍ ===
                        Toast.makeText(this, "Vítej zpět!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        // === CHYBA - DETAILNÍ ERROR HANDLING ===
                        val errorMessage = when (val exception = task.exception) {
                            // Uživatel neexistuje v databázi
                            is FirebaseAuthInvalidUserException -> {
                                "Tento účet neexistuje. Zkus se zaregistrovat."
                            }
                            // Špatné heslo
                            is FirebaseAuthInvalidCredentialsException -> {
                                "Nesprávné heslo. Zkus to znovu."
                            }
                            // Síťová chyba
                            is FirebaseNetworkException -> {
                                "Problém s připojením k internetu"
                            }
                            // Jiná chyba - zobraz obecnou zprávu
                            else -> {
                                "Chyba přihlášení: ${exception?.message}"
                            }
                        }

                        // Zobrazení chyby uživateli
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()

                        // === VRÁCENÍ BUTTONU DO PŮVODNÍHO STAVU ===
                        btnLogin.isEnabled = true
                        btnLogin.text = "PŘIHLÁSIT SE"
                    }
                }
        }

        // === PŘECHOD NA REGISTRACI ===
        tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish() // Ukončíme Login, aby se tam uživatel nevracel tlačítkem Zpět
        }
    }
}