package com.example.ukolnicek.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.ukolnicek.activities.LoginActivity
import com.example.ukolnicek.databinding.FragmentSettingsBinding
import com.example.ukolnicek.utils.PreferencesManager
import com.google.firebase.auth.FirebaseAuth

class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding
    private lateinit var prefs: PreferencesManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = PreferencesManager(requireContext())

        // Nastavení přepínače řazení
        binding.switchSort.isChecked = prefs.getSortOrder()
        
        binding.switchSort.setOnCheckedChangeListener { _, isChecked ->
            prefs.saveSortOrder(isChecked)
        }

        // Zobrazení emailu
        val user = FirebaseAuth.getInstance().currentUser
        binding.tvEmail.text = user?.email ?: "Nepřihlášen"

        // Odhlášení
        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }
    }
}
