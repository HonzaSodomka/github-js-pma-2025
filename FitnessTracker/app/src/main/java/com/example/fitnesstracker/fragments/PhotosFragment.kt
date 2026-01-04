package com.example.fitnesstracker.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnesstracker.activities.PhotoDetailActivity
import com.example.fitnesstracker.R
import com.example.fitnesstracker.adapters.PhotosAdapter
import com.example.fitnesstracker.database.AppDatabase
import com.example.fitnesstracker.database.PhotoEntity
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * Fragment pro photo galerii (progress tracking fotky)
 * - Lokální úložiště pomocí Room databáze
 * - Mřížka 3 sloupce (GridLayoutManager)
 * - Loading state při načítání
 * - Empty state pokud nemá fotky
 * - Validace a error handling
 */
class PhotosFragment : Fragment() {

    private lateinit var rvPhotos: RecyclerView
    private lateinit var fabAdd: ExtendedFloatingActionButton
    private lateinit var llEmptyState: LinearLayout
    private lateinit var progressBar: ProgressBar

    private lateinit var adapter: PhotosAdapter
    private lateinit var database: AppDatabase

    /**
     * Launcher pro výběr fotky z galerie
     * Používá moderní Activity Result API
     */
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri = result.data?.data

            // === VALIDACE ŽE JE VYBRANÝ OBRÁZEK ===
            if (imageUri == null) {
                Toast.makeText(context, "Nebyl vybrán žádný obrázek", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }

            // Uložení fotky
            saveImageLocally(imageUri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_photos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // === INICIALIZACE DATABÁZE (ROOM) ===
        database = AppDatabase.getDatabase(requireContext())

        // === UI KOMPONENTY ===
        rvPhotos = view.findViewById(R.id.rvPhotos)
        fabAdd = view.findViewById(R.id.fabAddPhoto)
        llEmptyState = view.findViewById(R.id.llEmptyState)
        progressBar = view.findViewById(R.id.progressBar)

        // === SETUP RECYCLERVIEW (MŘÍŽKA 3 SLOUPCE) ===
        rvPhotos.layoutManager = GridLayoutManager(context, 3)

        // Inicializace adapteru s callback pro kliknutí na fotku
        adapter = PhotosAdapter(emptyList()) { photo ->
            // Kliknutí na fotku -> Otevřít detail
            val intent = Intent(requireContext(), PhotoDetailActivity::class.java)
            intent.putExtra("FILE_PATH", photo.filePath)
            intent.putExtra("PHOTO_ID", photo.id)
            startActivity(intent)
        }
        rvPhotos.adapter = adapter

        // === FAB PRO PŘIDÁNÍ FOTKY ===
        fabAdd.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            pickImageLauncher.launch(intent)
        }

        // === SKRÝVÁNÍ FAB PŘI SCROLLOVÁNÍ (HEZKÝ EFEKT) ===
        rvPhotos.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) {
                    fabAdd.shrink() // Zmenší se jen na ikonu
                } else {
                    fabAdd.extend() // Roztáhne se s textem
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        // Vždy načíst znovu data, když se vrátíme (např. po smazání fotky v detailu)
        loadPhotos()
    }

    /**
     * Uloží obrázek lokálně do app storage
     * - Vytvoří unikátní název souboru (UUID)
     * - Zkopíruje data do app filesDir
     * - Uloží cestu do Room databáze
     */
    private fun saveImageLocally(sourceUri: Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // === A. VYTVOŘENÍ UNIKÁTNÍHO NÁZVU SOUBORU ===
                val filename = "progress_${UUID.randomUUID()}.jpg"
                val file = File(requireContext().filesDir, filename)

                // === B. ZKOPÍROVÁNÍ DAT ZE ZDROJE DO NAŠEHO SOUBORU ===
                val inputStream = requireContext().contentResolver.openInputStream(sourceUri)

                // Validace že se soubor otevřel
                if (inputStream == null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Chyba při čtení obrázku", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                val outputStream = FileOutputStream(file)

                inputStream.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }

                // === C. VYTVOŘENÍ ZÁZNAMU DO DATABÁZE ===
                val photoEntity = PhotoEntity(
                    filePath = file.absolutePath,
                    dateTimestamp = System.currentTimeMillis()
                )

                database.photoDao().insertPhoto(photoEntity)

                // === D. AKTUALIZACE UI NA HLAVNÍM VLÁKNĚ ===
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Fotka uložena!", Toast.LENGTH_SHORT).show()
                    loadPhotos() // Znovu načíst seznam
                }

            } catch (e: Exception) {
                // === ERROR HANDLING PRO UKLÁDÁNÍ ===
                withContext(Dispatchers.Main) {
                    val errorMessage = when (e) {
                        is java.io.FileNotFoundException -> "Soubor nebyl nalezen"
                        is java.io.IOException -> "Chyba při kopírování souboru"
                        is SecurityException -> "Nemáš oprávnění k souboru"
                        else -> "Chyba při ukládání: ${e.message}"
                    }
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    /**
     * Načte fotky z Room databáze
     * - Zobrazuje loading state
     * - Error handling
     * - Empty state pokud nemá fotky
     */
    private fun loadPhotos() {
        // === LOADING STATE START ===
        showLoading()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Získáme všechny fotky seřazené od nejnovější
                val photos = database.photoDao().getAllPhotos()

                withContext(Dispatchers.Main) {
                    adapter.updateData(photos)

                    // === PŘEPÍNÁNÍ MEZI SEZNAMEM A EMPTY STATEM ===
                    if (photos.isEmpty()) {
                        showEmptyState()
                    } else {
                        showContent()
                    }
                }

            } catch (e: Exception) {
                // === ERROR HANDLING PRO NAČÍTÁNÍ ===
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Chyba při načítání fotek: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()

                    // I při chybě zobraz empty state (lepší než bílá obrazovka)
                    showEmptyState()
                }
            }
        }
    }

    /**
     * Zobrazí loading indicator
     * Skryje RecyclerView i Empty State
     */
    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        rvPhotos.visibility = View.GONE
        llEmptyState.visibility = View.GONE
    }

    /**
     * Zobrazí empty state
     * Skryje loading i RecyclerView
     */
    private fun showEmptyState() {
        progressBar.visibility = View.GONE
        rvPhotos.visibility = View.GONE
        llEmptyState.visibility = View.VISIBLE
    }

    /**
     * Zobrazí RecyclerView s fotkami
     * Skryje loading i Empty State
     */
    private fun showContent() {
        progressBar.visibility = View.GONE
        rvPhotos.visibility = View.VISIBLE
        llEmptyState.visibility = View.GONE
    }
}