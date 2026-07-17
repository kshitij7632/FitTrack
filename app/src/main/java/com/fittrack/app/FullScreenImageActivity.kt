package com.fittrack.app

import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File

class FullScreenImageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen_image)

        val imagePath = intent.getStringExtra("IMAGE_PATH")
        val ivFullScreen = findViewById<ImageView>(R.id.ivFullScreen)
        val fabClose = findViewById<FloatingActionButton>(R.id.fabClose)

        if (imagePath != null) {
            val file = File(imagePath)
            if (file.exists()) {
                ivFullScreen.setImageURI(Uri.fromFile(file))
            }
        }

        fabClose.setOnClickListener {
            finish()
        }
    }
}
