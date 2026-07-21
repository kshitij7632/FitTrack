package com.fittrack.app

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.util.Calendar

class AddMeasurementActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var dbHelper: DatabaseHelper
    private var username = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_measurement)

        prefs = getSharedPreferences("FitTrackPrefs", MODE_PRIVATE)
        dbHelper = DatabaseHelper(this)
        username = prefs.getString("loggedInUser", "") ?: ""

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarAddMeasurement)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        // Pre-fill date/time
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        val timeSdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        val etDate = findViewById<TextInputEditText>(R.id.etMeasDate)
        val etTime = findViewById<TextInputEditText>(R.id.etMeasTime)
        etDate.setText(sdf.format(java.util.Date()))
        etTime.setText(timeSdf.format(java.util.Date()))

        // Date picker
        etDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(this, { _, y, m, d -> etDate.setText(String.format("%02d/%02d/%04d", d, m + 1, y)) },
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        // Time picker
        etTime.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(this, { _, h, min -> etTime.setText(String.format("%02d:%02d", h, min)) },
                cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }

        findViewById<MaterialButton>(R.id.btnSaveMeasurement).setOnClickListener { saveMeasurement() }
    }

    private fun saveMeasurement() {
        fun getDouble(id: Int) = findViewById<TextInputEditText>(id).text.toString().toDoubleOrNull() ?: 0.0
        fun getString(id: Int) = findViewById<TextInputEditText>(id).text.toString().trim()

        val date = getString(R.id.etMeasDate)
        val time = getString(R.id.etMeasTime)

        if (date.isEmpty()) { Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show(); return }

        val measurement = BodyMeasurement(
            username = username,
            date = date,
            time = time,
            bodyWeight = getDouble(R.id.etBodyWeight),
            chest = getDouble(R.id.etChest),
            waist = getDouble(R.id.etWaist),
            hips = getDouble(R.id.etHips),
            leftArm = getDouble(R.id.etLeftArm),
            rightArm = getDouble(R.id.etRightArm),
            leftForearm = getDouble(R.id.etLeftForearm),
            rightForearm = getDouble(R.id.etRightForearm),
            leftThigh = getDouble(R.id.etLeftThigh),
            rightThigh = getDouble(R.id.etRightThigh),
            leftCalf = getDouble(R.id.etLeftCalf),
            rightCalf = getDouble(R.id.etRightCalf),
            neck = getDouble(R.id.etNeck),
            shoulderWidth = getDouble(R.id.etShoulder),
            bodyFat = getDouble(R.id.etBodyFat),
            notes = getString(R.id.etMeasNotes)
        )

        dbHelper.insertMeasurement(measurement)
        Toast.makeText(this, "Measurements saved!", Toast.LENGTH_SHORT).show()
        overridePendingTransition(0, R.anim.slide_out_left)
        finish()
    }
}
