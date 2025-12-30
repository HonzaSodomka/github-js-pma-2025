package com.example.ukol13

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.ukol13.database.QuizDatabase
import com.example.ukol13.database.User
import com.example.ukol13.databinding.ActivityWelcomeBinding
import kotlinx.coroutines.launch

class WelcomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomeBinding
    private lateinit var database: QuizDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        database = QuizDatabase.getDatabase(this)

        binding.btnStart.setOnClickListener {
            val name = binding.tilName.editText?.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(this, "Zadej své jméno!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                // Zkontroluj, jestli uživatel už existuje
                val existingUser = database.userDao().getUserByName(name)

                val userId = if (existingUser != null) {
                    // Uživatel existuje - použij jeho ID
                    existingUser.id
                } else {
                    // Nový uživatel - vytvoř ho
                    database.userDao().insert(User(name = name)).toInt()
                }

                // Ulož ID do SharedPreferences
                val prefs = getSharedPreferences("QuizPrefs", MODE_PRIVATE)
                prefs.edit().putInt("USER_ID", userId).apply()

                // Přejdi na výběr kategorií
                val intent = Intent(this@WelcomeActivity, CategoryActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        binding.tvStats.setOnClickListener {
            val prefs = getSharedPreferences("QuizPrefs", MODE_PRIVATE)
            val savedUserId = prefs.getInt("USER_ID", 0)

            if (savedUserId != 0) {
                val intent = Intent(this, StatisticsActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Nejdřív si zahraj alespoň jednu hru!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}