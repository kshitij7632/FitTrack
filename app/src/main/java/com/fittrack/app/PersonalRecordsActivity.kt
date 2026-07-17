package com.fittrack.app

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar

class PersonalRecordsActivity : AppCompatActivity() {

    private lateinit var rvRecords: RecyclerView
    private lateinit var layoutEmptyRecords: LinearLayout
    private lateinit var adapter: PersonalRecordAdapter
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_records)

        dbHelper = DatabaseHelper(this)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarRecords)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { 
            finish()
            overridePendingTransition(android.R.anim.fade_in, R.anim.slide_out_left)
        }

        rvRecords = findViewById(R.id.rvRecords)
        layoutEmptyRecords = findViewById(R.id.layoutEmptyRecords)

        rvRecords.layoutManager = LinearLayoutManager(this)
        
        loadRecords()
    }

    private fun loadRecords() {
        val prefs = getSharedPreferences("FitTrackPrefs", MODE_PRIVATE)
        val username = prefs.getString("loggedInUser", "User") ?: "User"
        val records = dbHelper.getDetailedPersonalRecords(username)
        
        if (records.isEmpty()) {
            layoutEmptyRecords.visibility = View.VISIBLE
            rvRecords.visibility = View.GONE
        } else {
            layoutEmptyRecords.visibility = View.GONE
            rvRecords.visibility = View.VISIBLE
            adapter = PersonalRecordAdapter(records)
            rvRecords.adapter = adapter
        }
    }
}
