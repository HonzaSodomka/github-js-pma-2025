package com.example.ukol11

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.ukol11.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "💪 Fitness Tracker"

        // Zobraz Profil fragment jako výchozí
        supportFragmentManager.beginTransaction()
            .replace(binding.fragmentContainer.id, ProfilFragment())
            .commit()

        // Bottom Navigation listener
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_profil -> {
                    supportFragmentManager.beginTransaction()
                        .replace(binding.fragmentContainer.id, ProfilFragment())
                        .commit()
                    true
                }
                R.id.nav_treninky -> {
                    supportFragmentManager.beginTransaction()
                        .replace(binding.fragmentContainer.id, TreninkyFragment())
                        .commit()
                    true
                }
                R.id.nav_statistiky -> {
                    supportFragmentManager.beginTransaction()
                        .replace(binding.fragmentContainer.id, StatistikyFragment())
                        .commit()
                    true
                }
                else -> false
            }
        }

        // Nastav Profil jako vybraný
        binding.bottomNavigation.selectedItemId = R.id.nav_profil
    }
}