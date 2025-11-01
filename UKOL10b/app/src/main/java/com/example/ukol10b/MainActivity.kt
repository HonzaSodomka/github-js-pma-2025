package com.example.ukol10b

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.ukol10b.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var scoreHrac = 0
    private var scorePocitac = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnKamen.setOnClickListener { hraj("kamen") }
        binding.btnNuzky.setOnClickListener { hraj("nuzky") }
        binding.btnPapir.setOnClickListener { hraj("papir") }
    }

    private fun hraj(volbaHrace: String) {
        val moznosti = listOf("kamen", "nuzky", "papir")
        val volbaPocitace = moznosti.random()

        // Zobraz volbu počítače emoji
        binding.tvPocitac.text = when(volbaPocitace) {
            "kamen" -> "🪨"
            "nuzky" -> "✂️"
            else -> "📄"
        }

        // Urči vítěze
        val vysledek = when {
            volbaHrace == volbaPocitace -> "Remíza!"
            (volbaHrace == "kamen" && volbaPocitace == "nuzky") ||
                    (volbaHrace == "nuzky" && volbaPocitace == "papir") ||
                    (volbaHrace == "papir" && volbaPocitace == "kamen") -> {
                scoreHrac++
                "Vyhrál jsi! 🎉"
            }
            else -> {
                scorePocitac++
                "Prohrál jsi 😢"
            }
        }

        // Aktualizuj UI
        binding.tvVysledek.text = vysledek
        binding.tvScore.text = "Ty: $scoreHrac  |  Počítač: $scorePocitac"
    }
}