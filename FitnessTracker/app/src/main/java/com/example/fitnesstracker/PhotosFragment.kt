package com.example.fitnesstracker.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnesstracker.PhotoDetailActivity
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

class PhotosFragment : Fragment() {

    private lateinit var rvPhotos: RecyclerView
    private lateinit var fabAdd: ExtendedFloatingActionButton
    private lateinit var llEmptyState: LinearLayout

    private lateinit var adapter: PhotosAdapter
    private lateinit var database: AppDatabase

    // Launcher pro výběr fotky z galerie
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri = result.data?.data
            if (imageUri != null) {
                saveImageLocally(imageUri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Používáme náš nový layout s fitsSystemWindows="true"
        return inflater.inflate(R.layout.fragment_photos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializace databáze
        database = AppDatabase.getDatabase(requireContext())

        // Propojení UI prvků
        rvPhotos = view.findViewById(R.id.rvPhotos)
        fabAdd = view.findViewById(R.id.fabAddPhoto)
        llEmptyState = view.findViewById(R.id.llEmptyState)

        // Nastavení RecyclerView (mřížka, 2 sloupce vypadají lépe pro velké karty)
        rvPhotos.layoutManager = GridLayoutManager(context, 2)

        // Inicializace adaptéru s callbackem pro kliknutí
        adapter = PhotosAdapter(emptyList()) { photo ->
            // Kliknutí na fotku -> Otevřít detail
            val intent = Intent(requireContext(), PhotoDetailActivity::class.java)
            intent.putExtra("FILE_PATH", photo.filePath)
            intent.putExtra("PHOTO_ID", photo.id)
            startActivity(intent)
        }

        rvPhotos.adapter = adapter

        // Logika tlačítka Přidat
        fabAdd.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            pickImageLauncher.launch(intent)
        }

        // Skrývání FABu při scrollování (pro čistší vzhled)
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

    // DŮLEŽITÉ: Načítáme data v onResume, aby se seznam aktualizoval,
    // když se vrátíme z detailu (kde jsme mohli fotku smazat).
    override fun onResume() {
        super.onResume()
        loadPhotos()
    }

    // Funkce pro uložení fotky z galerie do aplikace
    private fun saveImageLocally(sourceUri: Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // 1. Vytvoříme unikátní název souboru
                val filename = "progress_${UUID.randomUUID()}.jpg"
                val file = File(requireContext().filesDir, filename)

                // 2. Zkopírujeme data ze zdroje do našeho souboru
                val inputStream = requireContext().contentResolver.openInputStream(sourceUri)
                val outputStream = FileOutputStream(file)

                inputStream?.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }

                // 3. Vytvoříme záznam do databáze
                val photoEntity = PhotoEntity(
                    filePath = file.absolutePath,
                    dateTimestamp = System.currentTimeMillis()
                )

                database.photoDao().insertPhoto(photoEntity)

                // 4. Aktualizujeme UI na hlavním vlákně
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Fotka úspěšně uložena!", Toast.LENGTH_SHORT).show()
                    loadPhotos() // Znovu načíst seznam
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Chyba při ukládání: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Funkce pro načtení fotek z databáze
    private fun loadPhotos() {
        lifecycleScope.launch(Dispatchers.IO) {
            // Získáme všechny fotky seřazené od nejnovější
            val photos = database.photoDao().getAllPhotos()

            withContext(Dispatchers.Main) {
                adapter.updateData(photos)

                // Přepínání mezi Seznamem a Empty Statem
                if (photos.isEmpty()) {
                    llEmptyState.visibility = View.VISIBLE
                    rvPhotos.visibility = View.GONE
                } else {
                    llEmptyState.visibility = View.GONE
                    rvPhotos.visibility = View.VISIBLE
                }
            }
        }
    }
}
