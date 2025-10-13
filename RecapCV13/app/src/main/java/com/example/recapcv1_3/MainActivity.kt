package com.example.recapcv1_3

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.recapcv1_3.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rgVyber.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbMargherita -> binding.ivPizza.setImageResource(R.drawable.marg)
                R.id.rbSalami -> binding.ivPizza.setImageResource(R.drawable.salami)
                R.id.rbHawaii -> binding.ivPizza.setImageResource(R.drawable.hawaii)
                R.id.rbQuattro -> binding.ivPizza.setImageResource(R.drawable.quattro)
            }
        }

        binding.btnObjednat.setOnClickListener {
            val pizza = when (binding.rgVyber.checkedRadioButtonId) {
                R.id.rbMargherita -> "Margherita"
                R.id.rbSalami -> "Salami"
                R.id.rbHawaii -> "Hawaii"
                R.id.rbQuattro -> "Quattro Formaggi"
                else -> "Žádná"
        }
            var extra = ""
            if (binding.cbSyr.isChecked) { extra += "Extra sýr " }
            if (binding.cbSunka.isChecked) { extra += "Extra šunka" }
            if (!binding.cbSyr.isChecked && !binding.cbSunka.isChecked) { extra += "Bez extra" }

            val jmeno = binding.etJmeno.text.toString()
            val prijmeni = binding.etPrijmeni.text.toString()
            val telefon = binding.etTelefon.text.toString()
            val adresa = binding.etAdresa.text.toString()

            var cenaPizza = when (binding.rgVyber.checkedRadioButtonId) {
                R.id.rbMargherita -> 129
                R.id.rbSalami -> 149
                R.id.rbHawaii -> 159
                R.id.rbQuattro -> 169
                else -> 0
            }

            if (binding.cbSyr.isChecked) {cenaPizza += 30}
            if (binding.cbSunka.isChecked) {cenaPizza += 40}

            binding.tvShrnuti.text = "Objednaná pizza $pizza pro $jmeno $prijmeni\n" +
                    "na adresu $adresa, telefon $telefon\n" +
                    "Speciální požadavky: $extra\n" +
                    "Celková cena $cenaPizza"
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}