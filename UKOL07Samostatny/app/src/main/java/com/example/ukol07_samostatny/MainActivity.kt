package com.example.ukol07_samostatny

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.ukol07_samostatny.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fragment1 = BMWFragment()
        val fragment2 = AudiFragment()
        val fragment3 = MercedesFragment()

        binding.rgCars.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbBMW -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.flCarContainer, fragment1)
                        .commit()
                }
                R.id.rbAudi -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.flCarContainer, fragment2)
                        .commit()
                }
                R.id.rbMerc -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.flCarContainer, fragment3)
                        .commit()
                }
            }
        }
    }
}