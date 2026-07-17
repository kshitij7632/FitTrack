package com.fittrack.app

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddWorkoutActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var etExerciseName: TextInputEditText
    private lateinit var spinnerMuscleGroup: Spinner
    private lateinit var etWeight: TextInputEditText
    private lateinit var etSets: TextInputEditText
    private lateinit var etReps: TextInputEditText
    private lateinit var etDuration: TextInputEditText
    private lateinit var etDate: TextInputEditText
    private lateinit var etNotes: TextInputEditText
    private lateinit var btnSaveWorkout: MaterialButton
    
    // Image handling
    private lateinit var ivWorkoutImagePreview: ImageView
    private lateinit var layoutImagePlaceholder: LinearLayout
    private lateinit var btnCamera: MaterialButton
    private lateinit var btnGallery: MaterialButton

    private var isEditMode = false
    private var workoutIdToEdit = -1
    private var currentPhotoPath: String = ""

    private val muscleGroups = arrayOf(
        "Chest", "Back", "Legs", "Shoulders", "Arms", "Core", "Cardio", "Full Body", "Other"
    )

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            updateImagePreview()
        } else {
            currentPhotoPath = "" // Reset if cancelled
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            if (uri != null) {
                currentPhotoPath = saveUriToInternalStorage(uri)
                updateImagePreview()
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(this, "Camera permission required.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_workout)

        dbHelper = DatabaseHelper(this)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarAddWorkout)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { 
            finish()
            overridePendingTransition(android.R.anim.fade_in, R.anim.slide_out_left)
        }

        initViews()
        setupSpinner()
        setupDatePicker()

        workoutIdToEdit = intent.getIntExtra("WORKOUT_ID", -1)
        if (workoutIdToEdit != -1) {
            isEditMode = true
            toolbar.title = getString(R.string.edit_workout_title)
            btnSaveWorkout.text = getString(R.string.btn_update_workout)
            loadWorkoutData(workoutIdToEdit)
        }

        btnSaveWorkout.setOnClickListener { saveWorkout() }
        
        btnCamera.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
        
        btnGallery.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryLauncher.launch(intent)
        }
    }

    private fun initViews() {
        etExerciseName = findViewById(R.id.etExerciseName)
        spinnerMuscleGroup = findViewById(R.id.spinnerMuscleGroup)
        etWeight = findViewById(R.id.etWeight)
        etSets = findViewById(R.id.etSets)
        etReps = findViewById(R.id.etReps)
        etDuration = findViewById(R.id.etDuration)
        etDate = findViewById(R.id.etDate)
        etNotes = findViewById(R.id.etNotes)
        btnSaveWorkout = findViewById(R.id.btnSaveWorkout)

        ivWorkoutImagePreview = findViewById(R.id.ivWorkoutImagePreview)
        layoutImagePlaceholder = findViewById(R.id.layoutImagePlaceholder)
        btnCamera = findViewById(R.id.btnCamera)
        btnGallery = findViewById(R.id.btnGallery)
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, muscleGroups)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerMuscleGroup.adapter = adapter
    }

    private fun setupDatePicker() {
        val calendar = Calendar.getInstance()
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            etDate.setText(sdf.format(calendar.time))
        }

        etDate.setOnClickListener {
            DatePickerDialog(
                this,
                R.style.Theme_FitTrack_DatePicker,
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        if (!isEditMode) {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            etDate.setText(sdf.format(calendar.time))
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: Exception) {
                null
            }
            photoFile?.also {
                val photoURI: Uri = FileProvider.getUriForFile(
                    this,
                    "${packageName}.fileprovider",
                    it
                )
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                cameraLauncher.launch(intent)
            }
        }
    }

    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun saveUriToInternalStorage(uri: Uri): String {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val file = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "IMG_$timeStamp.jpg")
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            return file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }

    private fun updateImagePreview() {
        if (currentPhotoPath.isNotEmpty()) {
            val file = File(currentPhotoPath)
            if (file.exists()) {
                ivWorkoutImagePreview.setImageURI(Uri.fromFile(file))
                layoutImagePlaceholder.visibility = View.GONE
            }
        } else {
            ivWorkoutImagePreview.setImageResource(R.drawable.ic_image)
            layoutImagePlaceholder.visibility = View.VISIBLE
        }
    }

    private fun loadWorkoutData(id: Int) {
        val workout = dbHelper.getWorkoutById(id)
        if (workout != null) {
            etExerciseName.setText(workout.exerciseName)
            val spinnerPosition = muscleGroups.indexOf(workout.muscleGroup)
            if (spinnerPosition >= 0) spinnerMuscleGroup.setSelection(spinnerPosition)
            etWeight.setText(workout.weight.toString())
            etSets.setText(workout.sets.toString())
            etReps.setText(workout.reps.toString())
            etDuration.setText(workout.duration.toString())
            etDate.setText(workout.date)
            etNotes.setText(workout.notes)
            currentPhotoPath = workout.imagePath
            updateImagePreview()
        }
    }

    private fun saveWorkout() {
        val name = etExerciseName.text.toString().trim()
        val weightStr = etWeight.text.toString().trim()
        val setsStr = etSets.text.toString().trim()
        val repsStr = etReps.text.toString().trim()
        val durationStr = etDuration.text.toString().trim()
        val date = etDate.text.toString().trim()

        if (name.isEmpty() || weightStr.isEmpty() || setsStr.isEmpty() || repsStr.isEmpty() || durationStr.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_empty_fields), Toast.LENGTH_SHORT).show()
            return
        }

        val weight = weightStr.toDoubleOrNull() ?: 0.0
        val sets = setsStr.toIntOrNull() ?: 0
        val reps = repsStr.toIntOrNull() ?: 0
        val duration = durationStr.toIntOrNull() ?: 0
        val muscleGroup = spinnerMuscleGroup.selectedItem.toString()
        val notes = etNotes.text.toString().trim()
        val prefs = getSharedPreferences("FitTrackPrefs", MODE_PRIVATE)
        val username = prefs.getString("loggedInUser", "User") ?: "User"

        val workout = Workout(
            id = if (isEditMode) workoutIdToEdit else 0,
            exerciseName = name,
            muscleGroup = muscleGroup,
            weight = weight,
            sets = sets,
            reps = reps,
            duration = duration,
            date = date,
            notes = notes,
            imagePath = currentPhotoPath,
            username = username
        )

        if (isEditMode) {
            dbHelper.updateWorkout(workout)
            Toast.makeText(this, getString(R.string.update_success), Toast.LENGTH_SHORT).show()
        } else {
            dbHelper.insertWorkout(workout)
            Toast.makeText(this, getString(R.string.save_success), Toast.LENGTH_SHORT).show()
        }

        finish()
    }
}
