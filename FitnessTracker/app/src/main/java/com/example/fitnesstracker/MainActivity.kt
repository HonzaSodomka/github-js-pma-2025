package com.example.fitnesstracker

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.fitnesstracker.databinding.ActivityMainBinding
import com.example.fitnesstracker.fragments.*
import com.google.firebase.auth.FirebaseAuth

/**
 * Hlavní aktivita aplikace
 * - Bottom Navigation pro přepínání mezi 5 fragmenty
 * - Hide/Show pattern místo replace (fragmenty přežívají)
 * - Double-back-press pro zavření aplikace
 * - Rotace zakázána v AndroidManifest.xml
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    // Instance fragmentů (vytvořeny jednou, pak se jen skrývají/zobrazují)
    private val workoutsFragment = WorkoutsFragment()
    private val exerciseFragment = ExerciseFragment()
    private val weightFragment = WeightFragment()
    private val photosFragment = PhotosFragment()
    private val profileFragment = ProfileFragment()

    // Sledujeme, který fragment je právě viditelný
    private var activeFragment: Fragment = workoutsFragment

    // Pro double-back-press
    private var backPressedTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // === KONTROLA PŘIHLÁŠENÍ ===
        auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // === INICIALIZACE FRAGMENTŮ ===
        // Při zakázané rotaci se onCreate volá jen jednou, takže tohle vždy platí
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().apply {
                add(R.id.fragmentContainer, profileFragment, "profile").hide(profileFragment)
                add(R.id.fragmentContainer, photosFragment, "photos").hide(photosFragment)
                add(R.id.fragmentContainer, weightFragment, "weight").hide(weightFragment)
                add(R.id.fragmentContainer, exerciseFragment, "exercises").hide(exerciseFragment)
                add(R.id.fragmentContainer, workoutsFragment, "workouts") // Tento bude vidět
            }.commit()
        }

        // === BOTTOM NAVIGATION LISTENER ===
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

    /**
     * Přepne mezi fragmenty pomocí hide/show
     * Fragmenty zůstávají v paměti a neničí se
     */
    private fun switchFragment(targetFragment: Fragment) {
        if (targetFragment == activeFragment) return

        // Fade animace při přepínání
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
            .hide(activeFragment)
            .show(targetFragment)
            .commit()

        activeFragment = targetFragment
    }
}