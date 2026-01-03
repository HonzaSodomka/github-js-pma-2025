package com.example.fitnesstracker.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.fitnesstracker.R
import com.example.fitnesstracker.database.PhotoEntity
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Přidali jsme parametr 'onPhotoClick' do konstruktoru
class PhotosAdapter(
    private var photos: List<PhotoEntity>,
    private val onPhotoClick: (PhotoEntity) -> Unit
) : RecyclerView.Adapter<PhotosAdapter.PhotoViewHolder>() {

    class PhotoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivPhoto: ImageView = view.findViewById(R.id.ivPhoto)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_photo, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val photo = photos[position]

        Glide.with(holder.itemView.context)
            .load(File(photo.filePath))
            .centerCrop()
            .into(holder.ivPhoto)

        val sdf = SimpleDateFormat("dd.MM.", Locale.getDefault())
        holder.tvDate.text = sdf.format(Date(photo.dateTimestamp))

        // Nastavení kliknutí na celou položku
        holder.itemView.setOnClickListener {
            onPhotoClick(photo)
        }
    }

    override fun getItemCount() = photos.size

    fun updateData(newPhotos: List<PhotoEntity>) {
        photos = newPhotos
        notifyDataSetChanged()
    }
}
