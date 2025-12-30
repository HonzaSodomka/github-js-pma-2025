package com.example.ukol13

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ukol13.adapter.GameResultAdapter
import com.example.ukol13.database.QuizDatabase
import com.example.ukol13.databinding.ActivityStatisticsBinding
import kotlinx.coroutines.launch

class StatisticsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStatisticsBinding
    private lateinit var database: QuizDatabase
    private lateinit var resultAdapter: GameResultAdapter
    private var userId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatisticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Statistiky"

        database = QuizDatabase.getDatabase(this)

        // Získej userId z SharedPreferences
        val prefs = getSharedPreferences("QuizPrefs", MODE_PRIVATE)
        userId = prefs.getInt("USER_ID", 0)

        if (userId == 0) {
            finish()
            return
        }

        loadUserStats()
        setupRecyclerView()
        loadGameHistory()

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun loadUserStats() {
        lifecycleScope.launch {
            val user = database.userDao().getUserById(userId)
            user?.let {
                binding.tvPlayerName.text = it.name
                binding.tvGamesPlayed.text = "Her odehráno: ${it.gamesPlayed}"
                binding.tvTotalScore.text = it.totalScore.toString()

                val avgScore = database.gameResultDao().getAverageScore(userId)
                binding.tvAvgScore.text = String.format("%.1f", avgScore ?: 0.0)
            }
        }
    }

    private fun setupRecyclerView() {
        lifecycleScope.launch {
            // Načti názvy kategorií
            val categories = database.categoryDao().getAllCategories()
            val categoryMap = mutableMapOf<Int, String>()

            categories.collect { list ->
                list.forEach { category ->
                    categoryMap[category.id] = category.name
                }

                resultAdapter = GameResultAdapter(categoryMap)

                binding.rvHistory.apply {
                    layoutManager = LinearLayoutManager(this@StatisticsActivity)
                    adapter = resultAdapter
                }
            }
        }
    }

    private fun loadGameHistory() {
        lifecycleScope.launch {
            database.gameResultDao().getResultsByUser(userId).collect { results ->
                resultAdapter.submitList(results)
            }
        }
    }
}