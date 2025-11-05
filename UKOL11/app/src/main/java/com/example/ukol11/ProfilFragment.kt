package com.example.ukol11

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.ukol11.databinding.FragmentProfilBinding
import com.google.android.material.snackbar.Snackbar

class ProfilFragment : Fragment() {
    private var _binding: FragmentProfilBinding? = null
    private val binding get() = _binding!!

    private var savedPhotoUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            savedPhotoUri = uri

            // ✅ DŮLEŽITÉ: Vezmi trvalé oprávnění k URI!
            try {
                requireActivity().contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                // Ignoruj chybu - některé URI nepodporují persistable permission
            }

            binding.ivProgressPhoto.setImageURI(uri)
            val sharedPref = requireActivity().getSharedPreferences("FitnessData", Context.MODE_PRIVATE)
            sharedPref.edit().putString("photoUri", uri.toString()).apply()
            Snackbar.make(binding.root, "📷 Foto uloženo!", Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPref = requireActivity().getSharedPreferences("FitnessData", Context.MODE_PRIVATE)

        // Načti uložená data
        binding.etJmeno.setText(sharedPref.getString("jmeno", ""))
        binding.etVyska.setText(sharedPref.getInt("vyska", 0).let { if (it == 0) "" else it.toString() })
        binding.etVaha.setText(sharedPref.getFloat("vaha", 0f).let { if (it == 0f) "" else it.toString() })

        // Načti BMI pokud existuje
        val savedBMI = sharedPref.getFloat("bmi", 0f)
        val savedKategorie = sharedPref.getString("bmiKategorie", "")
        if (savedBMI > 0) {
            binding.tvBMI.text = String.format("%.1f", savedBMI)
            binding.tvBMIKategorie.text = savedKategorie
        }

        // ✅ OPRAVENO: Bezpečné načítání fotky s try-catch
        val savedUri = sharedPref.getString("photoUri", null)
        if (savedUri != null) {
            try {
                savedPhotoUri = Uri.parse(savedUri)
                binding.ivProgressPhoto.setImageURI(savedPhotoUri)
            } catch (e: SecurityException) {
                // Oprávnění k URI už neplatí - vymaž uloženou URI
                sharedPref.edit().remove("photoUri").apply()
                Toast.makeText(requireContext(), "ℹ️ Fotka už není dostupná, vyber novou", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                // Jiná chyba - také vymaž URI
                sharedPref.edit().remove("photoUri").apply()
            }
        }

        // Tlačítko vybrat foto
        binding.btnVybratFoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // Tlačítko vypočítat BMI
        binding.btnVypocitatBMI.setOnClickListener {
            val vyskaText = binding.etVyska.text.toString()
            val vahaText = binding.etVaha.text.toString()

            if (vyskaText.isEmpty() || vahaText.isEmpty()) {
                Toast.makeText(requireContext(), "⚠️ Vyplň výšku a váhu!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val vyska = vyskaText.toDouble() / 100
            val vaha = vahaText.toDouble()
            val bmi = vaha / (vyska * vyska)

            val kategorie = when {
                bmi < 18.5 -> "Podváha"
                bmi < 25 -> "Normální váha"
                bmi < 30 -> "Nadváha"
                else -> "Obezita"
            }

            // Zobraz BMI
            binding.tvBMI.text = String.format("%.1f", bmi)
            binding.tvBMIKategorie.text = kategorie

            // Ulož BMI
            val editor = sharedPref.edit()
            editor.putFloat("bmi", bmi.toFloat())
            editor.putString("bmiKategorie", kategorie)
            editor.apply()

            Snackbar.make(binding.root, "🧮 BMI vypočítáno: ${String.format("%.1f", bmi)}", Snackbar.LENGTH_LONG).show()
        }

        // Tlačítko uložit profil
        binding.btnUlozitProfil.setOnClickListener {
            val jmeno = binding.etJmeno.text.toString()
            val vyskaText = binding.etVyska.text.toString()
            val vahaText = binding.etVaha.text.toString()

            if (jmeno.isEmpty() || vyskaText.isEmpty() || vahaText.isEmpty()) {
                Toast.makeText(requireContext(), "⚠️ Vyplň všechna pole!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val editor = sharedPref.edit()
            editor.putString("jmeno", jmeno)
            editor.putInt("vyska", vyskaText.toInt())
            editor.putFloat("vaha", vahaText.toFloat())
            editor.apply()

            Snackbar.make(binding.root, "💾 Profil uložen! 💪", Snackbar.LENGTH_LONG)
                .setAction("OK") { }
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}