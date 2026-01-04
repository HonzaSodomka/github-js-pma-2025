package com.example.fitnesstracker.activities

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
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.fitnesstracker.R
import com.example.fitnesstracker.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

/**
 * Aktivita pro zobrazení fotky na celou obrazovku
 * - Full-screen zobrazení s Glide
 * - Tlačítka Zpět a Smazat
 * - Edge-to-edge design
 * - Confirmation dialog při mazání
 * - Error handling
 */
class PhotoDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // === EDGE-TO-EDGE (MUSÍ BÝT PŘED setContentView) ===
        enableEdgeToEdge()

        setContentView(R.layout.activity_photo_detail)

        // === OŠETŘENÍ VÝŘEZU (NOTCH) A SYSTÉMOVÝCH LIŠT ===
        val rootLayout = findViewById<ConstraintLayout>(R.id.rootDetail)

        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Nastavíme padding, aby obsah nebyl pod kamerou nebo lištou dole
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // === NAČTENÍ DAT Z INTENTU ===
        val filePath = intent.getStringExtra("FILE_PATH")
        val photoId = intent.getIntExtra("PHOTO_ID", -1)

        // Validace dat
        if (filePath == null || photoId == -1) {
            Toast.makeText(this, "Chyba načítání fotky", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // === UI KOMPONENTY ===
        val ivPhoto = findViewById<ImageView>(R.id.ivFullPhoto)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnDelete = findViewById<ImageButton>(R.id.btnDelete)

        // === NAČTENÍ OBRÁZKU S GLIDE ===
        // Glide automaticky řeší loading, error states a memory management
        Glide.with(this)
            .load(File(filePath))
            .diskCacheStrategy(DiskCacheStrategy.NONE) // Neukládat do cache (progress fotky se můžou měnit)
            .skipMemoryCache(true) // Skip memory cache
            .error(R.drawable.ic_photos) // Fallback ikona při chybě načítání
            .into(ivPhoto)

        // === TLAČÍTKO ZPĚT ===
        btnBack.setOnClickListener {
            finish()
        }

        // === TLAČÍTKO SMAZAT ===
        btnDelete.setOnClickListener {
            showDeleteConfirmation(photoId, filePath)
        }
    }

    /**
     * Zobrazí confirmation dialog před smazáním
     * Varuje že akce je nevratná
     */
    private fun showDeleteConfirmation(photoId: Int, filePath: String) {
        AlertDialog.Builder(this)
            .setTitle("Smazat fotku?")
            .setMessage("Tato fotka bude trvale odstraněna. Akce je nevratná.")
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton("Smazat") { _, _ ->
                deletePhoto(photoId, filePath)
            }
            .setNegativeButton("Zrušit", null)
            .show()
    }

    /**
     * Smaže fotku z filesystému i databáze
     * - Smaže fyzický soubor
     * - Smaže záznam z Room databáze
     * - Error handling pro oba kroky
     */
    private fun deletePhoto(photoId: Int, filePath: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val db = AppDatabase.getDatabase(applicationContext)

                // === 1. SMAZÁNÍ FYZICKÉHO SOUBORU ===
                val file = File(filePath)
                var fileDeleted = false

                if (file.exists()) {
                    fileDeleted = file.delete()

                    // Error handling - soubor se nepodařilo smazat
                    if (!fileDeleted) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@PhotoDetailActivity,
                                "Nepodařilo se smazat soubor",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        // Pokračujeme dál a smažeme alespoň záznam z DB
                    }
                } else {
                    // Soubor už neexistuje (byl smazán ručně nebo jinou app)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@PhotoDetailActivity,
                            "Soubor již neexistuje",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                // === 2. SMAZÁNÍ Z DATABÁZE ===
                db.photoDao().deleteById(photoId)

                // === 3. ÚSPĚCH - ZAVŘÍT AKTIVITU ===
                withContext(Dispatchers.Main) {
                    val message = if (fileDeleted) "Smazáno" else "Záznam odebrán (soubor už neexistoval)"
                    Toast.makeText(this@PhotoDetailActivity, message, Toast.LENGTH_SHORT).show()
                    finish()
                }

            } catch (e: Exception) {
                // === ERROR HANDLING PRO CELOU OPERACI ===
                withContext(Dispatchers.Main) {
                    val errorMessage = when (e) {
                        is SecurityException -> "Nemáš oprávnění k smazání souboru"
                        is IOException -> "Chyba při mazání souboru"
                        else -> "Chyba při mazání: ${e.message}"
                    }
                    Toast.makeText(this@PhotoDetailActivity, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}