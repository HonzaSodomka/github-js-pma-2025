package com.example.cviceni10

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cviceni10.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var nahodneCislo = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Vygeneruj náhodné číslo při spuštění
        nahodneCislo = (1..10).random()

        binding.btnZkontroluj.setOnClickListener {
            val tipText = binding.etTip.text.toString()

            // Kontrola, jestli uživatel něco zadal
            if (tipText.isEmpty()) {
                Toast.makeText(this, "Zadej nějaké číslo!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val tip = tipText.toInt()

            if (tip == nahodneCislo) {
                Toast.makeText(this, "Správně! 🎉", Toast.LENGTH_SHORT).show()
                // Vygeneruj nové číslo
                nahodneCislo = (1..10).random()
                binding.etTip.text.clear()
            } else {
                Toast.makeText(this, "Vedle! Zkus to znovu.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}