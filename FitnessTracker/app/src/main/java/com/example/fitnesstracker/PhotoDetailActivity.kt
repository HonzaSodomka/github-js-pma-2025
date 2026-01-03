package com.example.fitnesstracker

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.fitnesstracker.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class PhotoDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Aktivace Edge-to-Edge (musí být před setContentView)
        enableEdgeToEdge()

        setContentView(R.layout.activity_photo_detail)

        // 2. Ošetření výřezu (Notch) a Systémových lišt
        val rootLayout = findViewById<ConstraintLayout>(R.id.rootDetail)

        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Nastavíme padding, aby obsah nebyl pod kamerou nebo lištou dole
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val filePath = intent.getStringExtra("FILE_PATH")
        val photoId = intent.getIntExtra("PHOTO_ID", -1)

        if (filePath == null || photoId == -1) {
            finish()
            return
        }

        val ivPhoto = findViewById<ImageView>(R.id.ivFullPhoto)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnDelete = findViewById<ImageButton>(R.id.btnDelete)

        Glide.with(this)
            .load(File(filePath))
            .into(ivPhoto)

        btnBack.setOnClickListener {
            finish()
        }

        btnDelete.setOnClickListener {
            showDeleteConfirmation(photoId, filePath)
        }
    }

    private fun showDeleteConfirmation(photoId: Int, filePath: String) {
        // Tady použijeme tmavý dialog, aby to nesvítilo do očí
        AlertDialog.Builder(this) // Pokud máš dark theme, bude automaticky tmavý
            .setTitle("Smazat fotku?")
            .setMessage("Tato akce je nevratná.")
            .setPositiveButton("Smazat") { _, _ ->
                deletePhoto(photoId, filePath)
            }
            .setNegativeButton("Zrušit", null)
            .show()
    }

    private fun deletePhoto(photoId: Int, filePath: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(applicationContext)

            // Smazání souboru
            val file = File(filePath)
            if (file.exists()) {
                file.delete()
            }

            // Smazání z DB
            db.photoDao().deleteById(photoId)

            withContext(Dispatchers.Main) {
                Toast.makeText(this@PhotoDetailActivity, "Smazáno", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
