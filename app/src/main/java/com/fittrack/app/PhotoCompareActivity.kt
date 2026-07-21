package com.fittrack.app

import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import java.io.File

class PhotoCompareActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var dbHelper: DatabaseHelper
    private var username = ""
    private var allPhotos = listOf<ProgressPhoto>()

    private lateinit var imgBefore: ImageView
    private lateinit var wrapperAfter: LinearLayout
    private lateinit var imgAfter: ImageView
    private lateinit var frameCompare: FrameLayout
    private lateinit var seekBarCompare: SeekBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_compare)

        prefs = getSharedPreferences("FitTrackPrefs", MODE_PRIVATE)
        dbHelper = DatabaseHelper(this)
        username = prefs.getString("loggedInUser", "") ?: ""

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarCompare)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        imgBefore = findViewById(R.id.imgBefore)
        wrapperAfter = findViewById(R.id.wrapperAfter)
        imgAfter = findViewById(R.id.imgAfter)
        frameCompare = findViewById(R.id.frameCompare)
        seekBarCompare = findViewById(R.id.seekBarCompare)

        loadPhotos()
    }

    private fun loadPhotos() {
        allPhotos = dbHelper.getProgressPhotos(username)
        if (allPhotos.isEmpty()) {
            return
        }

        val displayList = allPhotos.map { "${it.date} (${it.category.capitalize()})" }

        val spinnerBefore = findViewById<Spinner>(R.id.spinnerBefore)
        val spinnerAfter = findViewById<Spinner>(R.id.spinnerAfter)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, displayList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinnerBefore.adapter = adapter
        spinnerAfter.adapter = adapter

        // Set after to most recent, before to oldest (or previous)
        if (allPhotos.size > 1) {
            spinnerBefore.setSelection(allPhotos.size - 1) // Older
            spinnerAfter.setSelection(0) // Newer
        }

        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateImages()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        spinnerBefore.onItemSelectedListener = listener
        spinnerAfter.onItemSelectedListener = listener

        setupSlider()
    }

    private fun updateImages() {
        val spinnerBefore = findViewById<Spinner>(R.id.spinnerBefore)
        val spinnerAfter = findViewById<Spinner>(R.id.spinnerAfter)

        val posBefore = spinnerBefore.selectedItemPosition
        val posAfter = spinnerAfter.selectedItemPosition

        if (posBefore in allPhotos.indices) {
            val fileBefore = File(allPhotos[posBefore].filePath)
            if (fileBefore.exists()) {
                imgBefore.setImageURI(Uri.fromFile(fileBefore))
            }
        }

        if (posAfter in allPhotos.indices) {
            val fileAfter = File(allPhotos[posAfter].filePath)
            if (fileAfter.exists()) {
                imgAfter.setImageURI(Uri.fromFile(fileAfter))
            }
        }
    }

    private fun setupSlider() {
        // Run once layout is ready to set imgAfter width to match frameCompare width
        frameCompare.post {
            val frameWidth = frameCompare.width
            val imgParams = imgAfter.layoutParams
            imgParams.width = frameWidth
            imgAfter.layoutParams = imgParams

            val wrapParams = wrapperAfter.layoutParams
            wrapParams.width = frameWidth / 2
            wrapperAfter.layoutParams = wrapParams
        }

        seekBarCompare.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val frameWidth = frameCompare.width
                val newWidth = (frameWidth * progress) / 100
                val wrapParams = wrapperAfter.layoutParams
                wrapParams.width = newWidth
                wrapperAfter.layoutParams = wrapParams
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
}
