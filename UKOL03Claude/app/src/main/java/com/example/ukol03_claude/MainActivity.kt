package com.example.ukol03_claude

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.ukol03_claude.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rgVyberKol.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbKolo1 -> binding.ivBike.setImageResource(R.drawable.oiz_m10_tr)
                R.id.rbKolo2 -> binding.ivBike.setImageResource(R.drawable.oiz_m20_tr)
                R.id.rbKolo3 -> binding.ivBike.setImageResource(R.drawable.oiz_m30_tr)
            }
        }



        binding.btnObjednat.setOnClickListener {
            val vybraneKolo = when (binding.rgVyberKol.checkedRadioButtonId) {
                R.id.rbKolo1 -> "OIZ M10"
                R.id.rbKolo2 -> "OIZ M20"
                R.id.rbKolo3 -> "OIZ M30"
                else -> ""
            }

            val vidlice = binding.cbVidlice.isChecked
            val sedlo = binding.cbSedlo.isChecked
            val riditka = binding.cbRiditka.isChecked

            var souhrn = "Objednávka kola $vybraneKolo s rozšířením:"
            if (vidlice) souhrn += "\n Lepší vidlice"
            if (sedlo) souhrn += "\n Lepší sedlo"
            if (riditka) souhrn += "\n Karbonová řidítka"
            if (!vidlice && !sedlo && !riditka) souhrn += "\n bez rozšíření"
            binding.tvSouhrn.text = souhrn
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}