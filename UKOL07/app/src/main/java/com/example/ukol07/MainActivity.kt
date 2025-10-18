package com.example.ukol07

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.ukol07.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnFragment1.setOnClickListener {
            val fragment1 = Fragment1()
            supportFragmentManager.beginTransaction()
                .replace(R.id.flFrame, fragment1)
                .commit()
        }

        binding.btnFragment2.setOnClickListener {
            val fragment2 = Fragment2()
            supportFragmentManager.beginTransaction()
                .replace(R.id.flFrame, fragment2)
                .commit()
        }
    }
}