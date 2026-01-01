package com.example.ukol17

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.ukol17.databinding.FragmentSettingsBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var dataStoreManager: DataStoreManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dataStoreManager = DataStoreManager(requireContext())

        loadSettings()

        // Uložit nastavení
        binding.btnSave.setOnClickListener {
            saveSettings()
        }
    }

    private fun loadSettings() {
        viewLifecycleOwner.lifecycleScope.launch {
            val name = dataStoreManager.userName.first()
            val date = dataStoreManager.christmasDate.first()

            binding.etName.setText(name)
            binding.etDate.setText(date)
        }
    }

    private fun saveSettings() {
        val name = binding.etName.text.toString().trim()
        val date = binding.etDate.text.toString().trim()

        // Validace data (základní)
        if (date.isEmpty() || !date.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
            Toast.makeText(
                requireContext(),
                "Zadej datum ve formátu YYYY-MM-DD (např. 2025-12-24)",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        // Uložit
        viewLifecycleOwner.lifecycleScope.launch {
            dataStoreManager.saveUserName(name)
            dataStoreManager.saveChristmasDate(date)

            Toast.makeText(
                requireContext(),
                "✅ Nastavení uloženo!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}