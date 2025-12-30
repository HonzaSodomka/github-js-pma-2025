package com.example.ukol13

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ukol13.adapter.CategoryAdapter
import com.example.ukol13.database.QuizDatabase
import com.example.ukol13.databinding.ActivityCategoryBinding
import kotlinx.coroutines.launch

class CategoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryBinding
    private lateinit var database: QuizDatabase
    private lateinit var categoryAdapter: CategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "QuizMaster"

        database = QuizDatabase.getDatabase(this)

        setupRecyclerView()
        loadCategories()
    }

    private fun setupRecyclerView() {
        categoryAdapter = CategoryAdapter { category ->
            // Kliknutí na kategorii - spustí QuizActivity
            val intent = Intent(this, QuizActivity::class.java)
            intent.putExtra("CATEGORY_ID", category.id)
            intent.putExtra("CATEGORY_NAME", category.name)
            startActivity(intent)
        }

        binding.rvCategories.apply {
            layoutManager = LinearLayoutManager(this@CategoryActivity)
            adapter = categoryAdapter
        }
    }

    private fun loadCategories() {
        lifecycleScope.launch {
            database.categoryDao().getAllCategories().collect { categories ->
                categoryAdapter.submitList(categories)
            }
        }
    }
}