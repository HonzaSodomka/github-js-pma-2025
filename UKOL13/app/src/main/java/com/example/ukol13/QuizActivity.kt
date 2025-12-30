package com.example.ukol13

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.ukol13.database.GameResult
import com.example.ukol13.database.Question
import com.example.ukol13.database.QuizDatabase
import com.example.ukol13.databinding.ActivityQuizBinding
import kotlinx.coroutines.launch

class QuizActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuizBinding
    private lateinit var database: QuizDatabase

    private var questions: List<Question> = emptyList()
    private var currentQuestionIndex = 0
    private var score = 0
    private var categoryId = 0
    private var categoryName = ""
    private var userId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = QuizDatabase.getDatabase(this)

        // Získej kategorie z intentu
        categoryId = intent.getIntExtra("CATEGORY_ID", 0)
        categoryName = intent.getStringExtra("CATEGORY_NAME") ?: ""

        supportActionBar?.title = categoryName

        // Získej userId z SharedPreferences
        val prefs = getSharedPreferences("QuizPrefs", MODE_PRIVATE)
        userId = prefs.getInt("USER_ID", 0)

        // Načti otázky
        loadQuestions()

        // Nastavení kliknutí na tlačítka
        binding.btnOption1.setOnClickListener { checkAnswer(0) }
        binding.btnOption2.setOnClickListener { checkAnswer(1) }
        binding.btnOption3.setOnClickListener { checkAnswer(2) }
        binding.btnOption4.setOnClickListener { checkAnswer(3) }
    }

    private fun loadQuestions() {
        lifecycleScope.launch {
            // Načti 5 náhodných otázek z kategorie
            questions = database.questionDao().getRandomQuestions(categoryId, 5)

            if (questions.isEmpty()) {
                binding.tvQuestion.text = "Žádné otázky v této kategorii!"
                return@launch
            }

            displayQuestion()
        }
    }

    private fun displayQuestion() {
        if (currentQuestionIndex >= questions.size) {
            // Konec hry - přejdi na výsledky
            finishQuiz()
            return
        }

        val question = questions[currentQuestionIndex]

        // Aktualizuj UI
        binding.tvProgress.text = "Otázka ${currentQuestionIndex + 1}/${questions.size}"
        binding.tvScore.text = "Skóre: $score"
        binding.tvQuestion.text = question.questionText
        binding.btnOption1.text = question.option1
        binding.btnOption2.text = question.option2
        binding.btnOption3.text = question.option3
        binding.btnOption4.text = question.option4

        // Reset barev tlačítek
        resetButtonColors()

        // Povolení tlačítek
        enableButtons(true)
    }

    private fun checkAnswer(selectedOption: Int) {
        val question = questions[currentQuestionIndex]
        val buttons = listOf(binding.btnOption1, binding.btnOption2, binding.btnOption3, binding.btnOption4)

        // Zakaž tlačítka
        enableButtons(false)

        // Zkontroluj odpověď
        if (selectedOption == question.correctAnswer) {
            // Správná odpověď
            score++
            buttons[selectedOption].setBackgroundColor(Color.parseColor("#4CAF50")) // Zelená
        } else {
            // Špatná odpověď
            buttons[selectedOption].setBackgroundColor(Color.parseColor("#F44336")) // Červená
            buttons[question.correctAnswer].setBackgroundColor(Color.parseColor("#4CAF50")) // Ukaž správnou
        }

        // Po 1.5 sekundě další otázka
        Handler(Looper.getMainLooper()).postDelayed({
            currentQuestionIndex++
            displayQuestion()
        }, 1500)
    }

    private fun enableButtons(enabled: Boolean) {
        binding.btnOption1.isEnabled = enabled
        binding.btnOption2.isEnabled = enabled
        binding.btnOption3.isEnabled = enabled
        binding.btnOption4.isEnabled = enabled
    }

    private fun resetButtonColors() {
        val buttons = listOf(binding.btnOption1, binding.btnOption2, binding.btnOption3, binding.btnOption4)
        buttons.forEach {
            it.setBackgroundColor(Color.parseColor("#2196F3")) // Modrá
        }
    }

    private fun finishQuiz() {
        lifecycleScope.launch {
            // Ulož výsledek do databáze
            val result = GameResult(
                userId = userId,
                categoryId = categoryId,
                score = score,
                totalQuestions = questions.size
            )
            database.gameResultDao().insert(result)

            // Aktualizuj statistiky uživatele
            val user = database.userDao().getUserById(userId)
            user?.let {
                val updatedUser = it.copy(
                    totalScore = it.totalScore + score,
                    gamesPlayed = it.gamesPlayed + 1
                )
                database.userDao().update(updatedUser)
            }

            // Přejdi na ResultActivity
            val intent = Intent(this@QuizActivity, ResultActivity::class.java)
            intent.putExtra("SCORE", score)
            intent.putExtra("TOTAL", questions.size)
            intent.putExtra("CATEGORY_NAME", categoryName)
            startActivity(intent)
            finish()
        }
    }
}