package com.example.ukol01_claude

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

        val editJmeno = findViewById<EditText>(R.id.editTextJmeno)
        val editPrijmeni = findViewById<EditText>(R.id.editTextPrijmeni)
        val editVek = findViewById<EditText>(R.id.editTextVek)
        val editObec = findViewById<EditText>(R.id.editTextObec)
        val viewInformace = findViewById<TextView>(R.id.textViewVysledek)
        val butOdeslat = findViewById<Button>(R.id.buttonOdeslat)
        val butSmazat = findViewById<Button>(R.id.buttonSmazat)

        butOdeslat.setOnClickListener {
            val jmeno = editJmeno.text.toString()
            val prijmeni = editPrijmeni.text.toString()
            val obec = editObec.text.toString()
            val vek = editVek.text.toString()

            viewInformace.text = "Osoba $jmeno $prijmeni z obce $obec je stará $vek let"
        }

        butSmazat.setOnClickListener {
            editJmeno.text.clear()
            editPrijmeni.text.clear()
            editVek.text.clear()
            editObec.text.clear()
            viewInformace.text = ""
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}