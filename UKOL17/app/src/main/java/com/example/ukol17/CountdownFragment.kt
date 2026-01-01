package com.example.ukol17

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.ukol17.databinding.FragmentCountdownBinding
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class CountdownFragment : Fragment() {

    private var _binding: FragmentCountdownBinding? = null
    private val binding get() = _binding!!

    private lateinit var dataStoreManager: DataStoreManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCountdownBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dataStoreManager = DataStoreManager(requireContext())

        observeData()
    }

    private fun observeData() {
        // Sleduj datum Vánoc a jméno
        viewLifecycleOwner.lifecycleScope.launch {
            dataStoreManager.christmasDate.collect { dateString ->
                viewLifecycleOwner.lifecycleScope.launch {
                    dataStoreManager.userName.collect { name ->
                        updateCountdown(dateString, name)
                    }
                }
            }
        }
    }

    private fun updateCountdown(dateString: String, userName: String) {
        try {
            val today = LocalDate.now()
            val christmasDate = LocalDate.parse(dateString)

            // Výpočet dní
            val daysUntil = ChronoUnit.DAYS.between(today, christmasDate).toInt()

            // Aktualizuj UI
            binding.tvDays.text = daysUntil.toString()

            // Progress (od 1.12. do 24.12. = 24 dní)
            // Progress (od 24.12. loňského roku do 24.12. letošního)
            val lastChristmas = christmasDate.minusYears(1)  // 24.12. loňského roku
            val totalDays = ChronoUnit.DAYS.between(lastChristmas, christmasDate).toInt()  // 365 dní
            val daysPassed = ChronoUnit.DAYS.between(lastChristmas, today).toInt()
            val progress = if (totalDays > 0) {
                ((daysPassed.toFloat() / totalDays) * 100).toInt().coerceIn(0, 100)
            } else 0

            binding.progressBar.progress = progress
            binding.tvProgress.text = "$progress%"

            // Motivační zpráva
            val message = when {
                daysUntil < 0 -> "Vánoce už byly! 🎁"
                daysUntil == 0 -> "Dnes jsou Vánoce! 🎄🎅"
                daysUntil == 1 -> if (userName.isNotEmpty()) {
                    "Ahoj $userName! Zítra jsou Vánoce! 🎉"
                } else {
                    "Zítra jsou Vánoce! 🎉"
                }
                daysUntil <= 7 -> if (userName.isNotEmpty()) {
                    "Ahoj $userName! Už brzy budou Vánoce! 🎄"
                } else {
                    "Už brzy budou Vánoce! 🎄"
                }
                else -> if (userName.isNotEmpty()) {
                    "Ahoj $userName! Těš se na Vánoce! ⭐"
                } else {
                    "Těš se na Vánoce! ⭐"
                }
            }

            binding.tvMessage.text = message

        } catch (e: Exception) {
            binding.tvDays.text = "?"
            binding.tvMessage.text = "Nastavte datum Vánoc v Nastavení"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}