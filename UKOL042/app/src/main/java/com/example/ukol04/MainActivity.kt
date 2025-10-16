package com.example.ukol04

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.ukol04.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Top bar
        supportActionBar?.title = "Hlavní aktivita"

        binding.btnOdeslat.setOnClickListener {
            // Přečti všechna data
            val jmeno = binding.etJmeno.text.toString()
            val prijmeni = binding.etPrijmeni.text.toString()
            val vek = binding.etVek.text.toString()
            val mesto = binding.etMesto.text.toString()

            // Vytvoř Intent
            val intent = Intent(this, SecondActivity::class.java)

            // Přidej VŠECHNA data s RŮZNÝMI klíči
            intent.putExtra("JMENO", jmeno)
            intent.putExtra("PRIJMENI", prijmeni)
            intent.putExtra("VEK", vek)
            intent.putExtra("MESTO", mesto)

            // Spusť SecondActivity
            startActivity(intent)
        }
    }
}