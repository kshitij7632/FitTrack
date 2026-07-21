package com.fittrack.app

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PhotoGalleryActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var rvGallery: RecyclerView
    private lateinit var tvEmptyGallery: TextView
    private lateinit var currentPhotoPath: String
    private var username = "User"
    private var allPhotos = listOf<ProgressPhoto>()
    private var currentCategory = "All"

    private var pendingCategory = "front"

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            dbHelper.saveProgressPhoto(username, date, currentPhotoPath, category = pendingCategory)
            Toast.makeText(this, "Photo saved!", Toast.LENGTH_SHORT).show()
            loadPhotos()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_gallery)

        prefs = getSharedPreferences("FitTrackPrefs", MODE_PRIVATE)
        dbHelper = DatabaseHelper(this)
        username = prefs.getString("loggedInUser", "User") ?: "User"

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarGallery)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        rvGallery = findViewById(R.id.rvGallery)
        tvEmptyGallery = findViewById(R.id.tvEmptyGallery)
        
        rvGallery.layoutManager = GridLayoutManager(this, 3)

        findViewById<FloatingActionButton>(R.id.fabAddPhoto).setOnClickListener {
            showAddPhotoDialog()
        }

        findViewById<Button>(R.id.btnCompare).setOnClickListener {
            val intent = Intent(this, PhotoCompareActivity::class.java)
            startActivity(intent)
        }

        setupChips()
        loadPhotos()
    }

    private fun setupChips() {
        val chipGroup = findViewById<ChipGroup>(R.id.chipGroupCategory)
        chipGroup.check(R.id.chipCatAll)
        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            val chip = findViewById<Chip>(checkedIds[0])
            currentCategory = chip.text.toString()
            filterPhotos()
        }
    }

    private fun loadPhotos() {
        allPhotos = dbHelper.getProgressPhotos(username)
        filterPhotos()
    }

    private fun filterPhotos() {
        val filtered = if (currentCategory == "All") {
            allPhotos
        } else {
            allPhotos.filter { it.category.equals(currentCategory, ignoreCase = true) }
        }

        if (filtered.isEmpty()) {
            rvGallery.visibility = View.GONE
            tvEmptyGallery.visibility = View.VISIBLE
        } else {
            val adapter = PhotoAdapter(filtered) { path ->
                val intent = Intent(this, FullScreenImageActivity::class.java)
                intent.putExtra("IMAGE_PATH", path)
                startActivity(intent)
            }
            rvGallery.adapter = adapter
            rvGallery.visibility = View.VISIBLE
            tvEmptyGallery.visibility = View.GONE
        }
    }

    private fun showAddPhotoDialog() {
        val categories = arrayOf("Front", "Back", "Side")
        AlertDialog.Builder(this)
            .setTitle("Select Category")
            .setItems(categories) { _, which ->
                pendingCategory = categories[which].lowercase(Locale.getDefault())
                dispatchTakePictureIntent()
            }
            .show()
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    Toast.makeText(this, "Error creating file", Toast.LENGTH_SHORT).show()
                    null
                }
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.fittrack.app.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    takePictureLauncher.launch(takePictureIntent)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }
}
