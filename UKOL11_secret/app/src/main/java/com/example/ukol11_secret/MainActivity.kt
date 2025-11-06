package com.example.ukol11_secret

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.ukol11_secret.databinding.ActivityMainBinding
import com.example.ukol11_secret.fragments.ActiveTasksFragment
import com.example.ukol11_secret.fragments.CategoriesFragment
import com.example.ukol11_secret.fragments.CompletedTasksFragment
import com.google.android.material.tabs.TabLayout

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Task Manager"

        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Aktivní"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Dokončené"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Statistiky"))

        loadFragment(ActiveTasksFragment())

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> loadFragment(ActiveTasksFragment())
                    1 -> loadFragment(CompletedTasksFragment())
                    2 -> loadFragment(CategoriesFragment())
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        binding.fabAddTask.setOnClickListener {
            val intent = Intent(this, AddTaskActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        when (binding.tabLayout.selectedTabPosition) {
            0 -> loadFragment(ActiveTasksFragment())
            1 -> loadFragment(CompletedTasksFragment())
            2 -> loadFragment(CategoriesFragment())
        }
    }

    private fun loadFragment(fragment: androidx.fragment.app.Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}