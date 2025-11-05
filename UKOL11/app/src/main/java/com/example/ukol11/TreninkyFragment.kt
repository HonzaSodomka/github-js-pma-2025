package com.example.ukol11

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.ukol11.databinding.FragmentTreninkyBinding
import com.google.android.material.snackbar.Snackbar
import kotlin.random.Random

class TreninkyFragment : Fragment() {
    private var _binding: FragmentTreninkyBinding? = null
    private val binding get() = _binding!!

    private val motivacniCitaty = listOf(
        "💪 Úspěch začíná tam, kde končí pohodlí!",
        "🔥 Žádná bolest, žádný zisk!",
        "⚡ Tvé tělo slyší vše, co tvá mysl říká!",
        "🏆 Dnes je ten den!",
        "💯 Tvrdá práce se vyplácí!",
        "🎯 Nevzdávej to, pokud to stojí za to!",
        "🚀 Limity existují jen v tvé hlavě!",
        "💥 Buď silnější než tvoje výmluvy!",
        "⭐ Každý den je příležitost být lepší!",
        "🔋 Push yourself, nobody else will do it!"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTreninkyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPref = requireActivity().getSharedPreferences("FitnessData", Context.MODE_PRIVATE)

        // Načti tréninky
        nactiTreninky()

        // Náhodný motivační citát při kliknutí na card
        binding.cardMotivace.setOnClickListener {
            val randomCitat = motivacniCitaty[Random.nextInt(motivacniCitaty.size)]
            binding.tvMotivace.text = randomCitat
        }

        // Zobraz náhodný citát hned
        binding.tvMotivace.text = motivacniCitaty[Random.nextInt(motivacniCitaty.size)]

        // Tlačítko uložit trénink
        binding.btnUlozitTrenink.setOnClickListener {
            val datum = binding.etDatum.text.toString()
            val popis = binding.etPopis.text.toString()

            if (datum.isEmpty() || popis.isEmpty()) {
                Toast.makeText(requireContext(), "⚠️ Vyplň datum a popis!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Načti současnou historii
            val aktualniHistorie = sharedPref.getString("treninky", "") ?: ""

            // Přidej nový trénink
            val novyTrenink = "📅 $datum\n💪 $popis"
            val novaHistorie = if (aktualniHistorie.isEmpty()) {
                novyTrenink
            } else {
                "$aktualniHistorie\n\n━━━━━━━━━━━━━━━\n\n$novyTrenink"
            }

            // Ulož
            sharedPref.edit().putString("treninky", novaHistorie).apply()

            // Vymaž pole
            binding.etDatum.text?.clear()
            binding.etPopis.text?.clear()

            // Načti znovu
            nactiTreninky()

            // Náhodný motivační citát
            val randomCitat = motivacniCitaty[Random.nextInt(motivacniCitaty.size)]

            Snackbar.make(binding.root, "🔥 Trénink uložen! $randomCitat", Snackbar.LENGTH_LONG)
                .setAction("OK") { }
                .show()
        }

        // Tlačítko smazat vše
        binding.btnSmazatVse.setOnClickListener {
            // Dialog pro potvrzení
            AlertDialog.Builder(requireContext())
                .setTitle("⚠️ Smazat všechny tréninky?")
                .setMessage("Opravdu chceš smazat celou historii? Toto nelze vrátit zpět!")
                .setPositiveButton("Ano, smazat") { _, _ ->
                    sharedPref.edit().putString("treninky", "").apply()
                    nactiTreninky()
                    Toast.makeText(requireContext(), "🗑️ Historie vymazána", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Zrušit", null)
                .show()
        }
    }

    private fun nactiTreninky() {
        val sharedPref = requireActivity().getSharedPreferences("FitnessData", Context.MODE_PRIVATE)
        val treninky = sharedPref.getString("treninky", "")

        if (treninky.isNullOrEmpty()) {
            binding.tvSeznamTreninku.text = "Zatím žádné tréninky...\n\n💪 Začni cvičit a zaznamenej si první trénink!"
        } else {
            binding.tvSeznamTreninku.text = treninky
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}