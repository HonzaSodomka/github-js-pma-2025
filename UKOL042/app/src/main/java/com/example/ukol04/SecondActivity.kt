package com.example.ukol04

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.ukol04.databinding.ActivitySecondBinding

class SecondActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySecondBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecondBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Top bar
        supportActionBar?.title = "Druhá aktivita"

        // Přečti data z intentu
        val jmeno = intent.getStringExtra("JMENO")
        val prijmeni = intent.getStringExtra("PRIJMENI")
        val vek = intent.getStringExtra("VEK")
        val mesto = intent.getStringExtra("MESTO")

        // Zobraz je v TextViewech
        binding.tvJmeno.text = "Jméno: $jmeno"
        binding.tvPrijmeni.text = "Příjmení: $prijmeni"
        binding.tvVek.text = "Věk: $vek"
        binding.tvMesto.text = "Město: $mesto"

        // Button na třetí aktivitu
        binding.btnPosledDal.setOnClickListener {
            val souhrn = "$jmeno $prijmeni, $vek let, $mesto"

            val intent = Intent(this, ThirdActivity::class.java)
            intent.putExtra("SOUHRN", souhrn)
            startActivity(intent)
        }
    }
}