package com.example.ukol05

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.ukol05.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import android.view.inputmethod.InputMethodManager
import android.content.Context

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Toast a Snackbar"

        binding.btnPlus.setOnClickListener {
            val cislo1 = binding.etCislo1.text.toString()
            val cislo2 = binding.etCislo2.text.toString()
            val result = (cislo1.toDouble() + cislo2.toDouble()).toString()

            binding.tvCislo.text = result

            val toast = Toast(this)
            val layout = layoutInflater.inflate(R.layout.custom_toast, null)
            val icon = layout.findViewById<ImageView>(R.id.toast_icon)
            val text = layout.findViewById<TextView>(R.id.toast_text)

            icon.setImageResource(R.drawable.ic_plus)
            text.text = "Sčítání dokončeno"

            toast.view = layout
            toast.duration = Toast.LENGTH_LONG
            toast.show()
        }

        binding.btnMinus.setOnClickListener {
            val cislo1 = binding.etCislo1.text.toString()
            val cislo2 = binding.etCislo2.text.toString()
            val result = (cislo1.toDouble() - cislo2.toDouble()).toString()

            binding.tvCislo.text = result

            val toast = Toast(this)
            val layout = layoutInflater.inflate(R.layout.custom_toast, null)
            val icon = layout.findViewById<ImageView>(R.id.toast_icon)
            val text = layout.findViewById<TextView>(R.id.toast_text)

            icon.setImageResource(R.drawable.ic_minus)
            text.text = "Odčítání dokončeno"

            toast.view = layout
            toast.duration = Toast.LENGTH_LONG
            toast.show()
        }

        binding.btnKrat.setOnClickListener {
            val cislo1 = binding.etCislo1.text.toString()
            val cislo2 = binding.etCislo2.text.toString()
            val result = (cislo1.toDouble() * cislo2.toDouble()).toString()

            binding.tvCislo.text = result

            val toast = Toast(this)
            val layout = layoutInflater.inflate(R.layout.custom_toast, null)
            val icon = layout.findViewById<ImageView>(R.id.toast_icon)
            val text = layout.findViewById<TextView>(R.id.toast_text)

            icon.setImageResource(R.drawable.ic_krat)
            text.text = "Násobení dokončeno"

            toast.view = layout
            toast.duration = Toast.LENGTH_LONG
            toast.show()
        }

        binding.btnDeleno.setOnClickListener {
            val cislo1 = binding.etCislo1.text.toString()
            val cislo2 = binding.etCislo2.text.toString()
            val result = (cislo1.toDouble() / cislo2.toDouble()).toString()

            binding.tvCislo.text = result

            val toast = Toast(this)
            val layout = layoutInflater.inflate(R.layout.custom_toast, null)
            val icon = layout.findViewById<ImageView>(R.id.toast_icon)
            val text = layout.findViewById<TextView>(R.id.toast_text)

            icon.setImageResource(R.drawable.ic_deleno)
            text.text = "Dělení dokončeno"

            toast.view = layout
            toast.duration = Toast.LENGTH_LONG
            toast.show()
        }

        binding.btnSnack.setOnClickListener {
            hideKeyboard()
            val vypocet = binding.tvCislo.text.toString()
            val cislo1 = binding.etCislo1.text.toString()
            val cislo2 = binding.etCislo2.text.toString()
            binding.tvCislo.text = "XXX"
            binding.etCislo1.text.clear()
            binding.etCislo2.text.clear()
            Snackbar.make(binding.root, "Výsledek vymazán", Snackbar.LENGTH_LONG).setAction("Zrušit vymazání"){
                binding.tvCislo.text = vypocet
                binding.etCislo1.setText(cislo1)
                binding.etCislo2.setText(cislo2)
            }.show()
        }
    }
}