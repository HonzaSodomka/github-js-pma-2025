package com.example.ukol08

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.ukol08.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPref = getSharedPreferences("MojeData", MODE_PRIVATE)

        binding.btnUlozit.setOnClickListener {
            val jmeno = binding.etJmeno.text.toString()
            val vek = binding.etVek.text.toString().toInt()
            val checked = binding.cbPotvrzeni.isChecked

            val editor = sharedPref.edit()

            editor.putString("jmeno", jmeno)
            editor.putInt("vek", vek)
            editor.putBoolean("checked", checked)
            editor.apply()
        }

        binding.btnNacist.setOnClickListener {
            binding.etJmeno.setText(sharedPref.getString("jmeno", "Beze jmena"))
            binding.etVek.setText(sharedPref.getInt("vek", 0).toString())
            binding.cbPotvrzeni.isChecked = sharedPref.getBoolean("checked", false)
        }

    }

}