package com.example.ukol04

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.ukol04.databinding.ActivityThirdBinding

class ThirdActivity : AppCompatActivity() {

    private lateinit var binding: ActivityThirdBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityThirdBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Top bar
        supportActionBar?.title = "Třetí aktivita"

        // Přijmi data
        val souhrn = intent.getStringExtra("SOUHRN")

        // Zobraz
        binding.tvSouhrn.text = souhrn
    }
}