package com.fittrack.app

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

/**
 * Simple stub for split creation/editing.
 * Full implementation in next phase.
 */
class SplitEditorActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var dbHelper: DatabaseHelper
    private var username = ""
    private var splitId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_split_editor)

        prefs = getSharedPreferences("FitTrackPrefs", MODE_PRIVATE)
        dbHelper = DatabaseHelper(this)
        username = prefs.getString("loggedInUser", "") ?: ""
        splitId = intent.getIntExtra("SPLIT_ID", -1)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarSplitEditor)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        if (splitId > 0) {
            toolbar.title = "Edit Split"
            loadSplit()
        } else {
            toolbar.title = "Create Custom Split"
        }

        findViewById<MaterialButton>(R.id.btnSaveSplit).setOnClickListener { saveSplit() }
    }

    private fun loadSplit() {
        val split = dbHelper.getSplitById(splitId) ?: return
        try {
            findViewById<TextInputEditText>(R.id.etSplitName).setText(split.name)
            findViewById<TextInputEditText>(R.id.etSplitDescription).setText(split.description)
            findViewById<TextInputEditText>(R.id.etSplitGoal).setText(split.goal)
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun saveSplit() {
        val name = try { findViewById<TextInputEditText>(R.id.etSplitName).text.toString().trim() } catch (e: Exception) { "" }
        val description = try { findViewById<TextInputEditText>(R.id.etSplitDescription).text.toString().trim() } catch (e: Exception) { "" }
        val goal = try { findViewById<TextInputEditText>(R.id.etSplitGoal).text.toString().trim() } catch (e: Exception) { "" }

        if (name.isEmpty()) { Toast.makeText(this, "Please enter a split name", Toast.LENGTH_SHORT).show(); return }

        if (splitId > 0) {
            dbHelper.updateSplit(splitId, username, name, description, goal)
            Toast.makeText(this, "Split updated!", Toast.LENGTH_SHORT).show()
        } else {
            val newId = dbHelper.insertSplit(username, name, description, goal)
            Toast.makeText(this, "Split created! Add workout days next.", Toast.LENGTH_SHORT).show()
            val intent = android.content.Intent(this, WeeklyScheduleActivity::class.java)
            intent.putExtra("SPLIT_ID", newId.toInt())
            startActivity(intent)
        }
        finish()
    }
}
