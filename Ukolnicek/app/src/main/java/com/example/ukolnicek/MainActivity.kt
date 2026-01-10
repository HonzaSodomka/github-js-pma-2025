package com.example.ukolnicek

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.ukolnicek.activities.LoginActivity
import com.example.ukolnicek.databinding.ActivityMainBinding
import com.example.ukolnicek.fragments.ActiveTasksFragment
import com.example.ukolnicek.fragments.CompletedTasksFragment
import com.example.ukolnicek.fragments.SettingsFragment
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    private val activeTasksFragment = ActiveTasksFragment()
    private val completedTasksFragment = CompletedTasksFragment()
    private val settingsFragment = SettingsFragment()

    private var activeFragment: Fragment = activeTasksFragment

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

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().apply {
                add(R.id.fragmentContainer, settingsFragment, "settings").hide(settingsFragment)
                add(R.id.fragmentContainer, completedTasksFragment, "completed").hide(completedTasksFragment)
                add(R.id.fragmentContainer, activeTasksFragment, "active")
            }.commit()
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_active -> switchFragment(activeTasksFragment)
                R.id.nav_completed -> switchFragment(completedTasksFragment)
                R.id.nav_settings -> switchFragment(settingsFragment)
            }
            true
        }
    }

    private fun switchFragment(targetFragment: Fragment) {
        if (targetFragment == activeFragment) return

        supportFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .hide(activeFragment)
            .show(targetFragment)
            .commit()

        activeFragment = targetFragment
    }
}
