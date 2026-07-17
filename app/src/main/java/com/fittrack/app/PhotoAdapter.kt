package com.fittrack.app

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class PhotoAdapter(
    private val photos: List<Pair<String, String>>, // (date, path)
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {

    class PhotoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivPhoto: ImageView = view.findViewById(R.id.ivPhoto)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_photo, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val photo = photos[position]
        holder.tvDate.text = photo.first
        
        val file = File(photo.second)
        if (file.exists()) {
            holder.ivPhoto.setImageURI(Uri.fromFile(file))
        } else {
            holder.ivPhoto.setImageResource(R.drawable.ic_image)
        }
        
        holder.itemView.setOnClickListener {
            onClick(photo.second)
        }
    }

    override fun getItemCount() = photos.size
}
