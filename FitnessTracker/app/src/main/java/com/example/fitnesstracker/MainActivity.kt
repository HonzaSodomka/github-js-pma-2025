package com.example.fitnesstracker

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.fitnesstracker.databinding.ActivityMainBinding
import com.example.fitnesstracker.fragments.*
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    // 1. Vytvoříme instance fragmentů (držíme je v paměti)
    private val workoutsFragment = WorkoutsFragment()
    private val exerciseFragment = ExerciseFragment()
    private val weightFragment = WeightFragment()
    private val photosFragment = PhotosFragment()
    private val profileFragment = ProfileFragment()

    // Sledujeme, který fragment je právě vidět
    private var activeFragment: Fragment = workoutsFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // 2. Inicializace fragmentů (všechny přidáme, ale skryjeme ty, co nejsou startovací)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().apply {
                add(R.id.fragmentContainer, profileFragment, "profile").hide(profileFragment)
                add(R.id.fragmentContainer, photosFragment, "photos").hide(photosFragment)
                add(R.id.fragmentContainer, weightFragment, "weight").hide(weightFragment)
                add(R.id.fragmentContainer, exerciseFragment, "exercises").hide(exerciseFragment)
                add(R.id.fragmentContainer, workoutsFragment, "workouts") // Tento bude vidět
            }.commit()
        }

        // 3. Přepínání pomocí hide/show
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_workouts -> switchFragment(workoutsFragment)
                R.id.nav_exercises -> switchFragment(exerciseFragment)
                R.id.nav_weight -> switchFragment(weightFragment)
                R.id.nav_photos -> switchFragment(photosFragment)
                R.id.nav_profile -> switchFragment(profileFragment)
            }
            true
        }
    }

    private fun switchFragment(targetFragment: Fragment) {
        if (targetFragment == activeFragment) return

        supportFragmentManager.beginTransaction()
            .hide(activeFragment)
            .show(targetFragment)
            .commit()

        activeFragment = targetFragment
    }
}
