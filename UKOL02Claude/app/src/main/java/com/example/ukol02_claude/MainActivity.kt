package com.example.ukol02_claude

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val vaha = findViewById<EditText>(R.id.etVaha)
        val vyska = findViewById<EditText>(R.id.etVyska)
        val vypocitat = findViewById<Button>(R.id.btnVypocitat)
        val smazat = findViewById<Button>(R.id.btnSmazat)
        val vysledek = findViewById<TextView>(R.id.tvResult)
        val kategorie = findViewById<TextView>(R.id.tvCategory)

        vypocitat.setOnClickListener {
            val vahaText = vaha.text.toString()
            val vyskaText = vyska.text.toString()
            val vahaKg = vahaText.toDouble()
            val vyskaCm = vyskaText.toDouble()
            val vyskaM = vyskaCm / 100

            val bmi = vahaKg / (vyskaM * vyskaM)

            val kat = when {
                bmi < 18.5 -> "Podváha"
                bmi < 25 -> "Normální váha"
                bmi < 30 -> "Nadváha"
                else -> "Obezita:("
            }

            vysledek.text = "Vaše BMI: ${String.format("%.1f", bmi)}"
            kategorie.text = "Kategorie: $kat"

        }
        smazat.setOnClickListener {
            vysledek.text = "Pro výpočet BMI zadejte hodnoty a klikněte na tlačítko"
            kategorie.text = "-----"
            vyska.text.clear()
            vaha.text.clear()

        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}