package com.example.ukol11

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.ukol11.databinding.FragmentStatistikyBinding
import kotlin.random.Random

class StatistikyFragment : Fragment() {
    private var _binding: FragmentStatistikyBinding? = null
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
        _binding = FragmentStatistikyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPref = requireActivity().getSharedPreferences("FitnessData", Context.MODE_PRIVATE)

        // Načti data
        nactiStatistiky(sharedPref)

        // Zobraz náhodný motivační citát
        binding.tvRandomMotivace.text = motivacniCitaty[Random.nextInt(motivacniCitaty.size)]

        // Tlačítko pro nový citát
        binding.btnNovyMotivcak.setOnClickListener {
            binding.tvRandomMotivace.text = motivacniCitaty[Random.nextInt(motivacniCitaty.size)]
        }
    }

    private fun nactiStatistiky(sharedPref: android.content.SharedPreferences) {
        // Počet tréninků
        val treninky = sharedPref.getString("treninky", "") ?: ""
        val pocetTreninku = if (treninky.isEmpty()) 0 else treninky.split("\n\n━━━━━━━━━━━━━━━\n\n").size
        binding.tvPocetTreninku.text = pocetTreninku.toString()

        // BMI
        val bmi = sharedPref.getFloat("bmi", 0f)
        binding.tvAktualniBMI.text = if (bmi > 0) String.format("%.1f", bmi) else "---"

        // Osobní údaje
        val jmeno = sharedPref.getString("jmeno", "---")
        val vyska = sharedPref.getInt("vyska", 0)
        val vaha = sharedPref.getFloat("vaha", 0f)

        binding.tvJmeno.text = "👤 Jméno: $jmeno"
        binding.tvVyska.text = "📏 Výška: ${if (vyska > 0) "$vyska cm" else "---"}"
        binding.tvVaha.text = "⚖️ Váha: ${if (vaha > 0) "$vaha kg" else "---"}"
    }

    override fun onResume() {
        super.onResume()
        // Aktualizuj statistiky při návratu do fragmentu
        val sharedPref = requireActivity().getSharedPreferences("FitnessData", Context.MODE_PRIVATE)
        nactiStatistiky(sharedPref)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}