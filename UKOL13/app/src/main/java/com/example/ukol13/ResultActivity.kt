package com.example.ukol13

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.ukol13.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        // Získej data z intentu
        val score = intent.getIntExtra("SCORE", 0)
        val total = intent.getIntExtra("TOTAL", 0)
        val categoryName = intent.getStringExtra("CATEGORY_NAME") ?: ""
        val categoryId = intent.getIntExtra("CATEGORY_ID", 0)

        // Zobraz výsledky
        binding.tvCategory.text = categoryName
        binding.tvScore.text = "$score/$total"

        // Zobraz motivační zprávu podle výsledku
        val percentage = (score.toFloat() / total.toFloat()) * 100
        binding.tvMessage.text = when {
            percentage == 100f -> "🌟 Perfektní výsledek!"
            percentage >= 80f -> "🎉 Skvělé!"
            percentage >= 60f -> "👍 Dobrá práce!"
            percentage >= 40f -> "💪 Můžeš to zlepšit!"
            else -> "📚 Zkus to znovu!"
        }

        // Tlačítko - hrát znovu stejnou kategorii
        binding.btnPlayAgain.setOnClickListener {
            val intent = Intent(this, QuizActivity::class.java)
            intent.putExtra("CATEGORY_ID", categoryId)
            intent.putExtra("CATEGORY_NAME", categoryName)
            startActivity(intent)
            finish()
        }

        // Tlačítko - zpět na kategorie
        binding.btnCategories.setOnClickListener {
            val intent = Intent(this, CategoryActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}