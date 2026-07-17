package com.fittrack.app

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class RegisterActivity : AppCompatActivity() {

    private lateinit var tilUsername: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var tilConfirmPassword: TextInputLayout
    private lateinit var etUsername: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var btnRegister: MaterialButton
    private lateinit var tvGoToLogin: android.widget.TextView
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        prefs = getSharedPreferences("FitTrackPrefs", MODE_PRIVATE)

        tilUsername = findViewById(R.id.tilRegUsername)
        tilPassword = findViewById(R.id.tilRegPassword)
        tilConfirmPassword = findViewById(R.id.tilRegConfirmPassword)
        etUsername = findViewById(R.id.etRegUsername)
        etPassword = findViewById(R.id.etRegPassword)
        etConfirmPassword = findViewById(R.id.etRegConfirmPassword)
        btnRegister = findViewById(R.id.btnRegister)
        tvGoToLogin = findViewById(R.id.tvGoToLogin)

        btnRegister.setOnClickListener { registerUser() }
        tvGoToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun registerUser() {
        tilUsername.error = null
        tilPassword.error = null
        tilConfirmPassword.error = null

        val rawUsername = etUsername.text.toString().trim()
        val username = rawUsername.lowercase()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        if (rawUsername.isEmpty()) {
            tilUsername.error = getString(R.string.error_username_required)
            etUsername.requestFocus()
            return
        }
        if (rawUsername.length < 3) {
            tilUsername.error = "Username must be at least 3 characters"
            etUsername.requestFocus()
            return
        }
        if (password.isEmpty()) {
            tilPassword.error = getString(R.string.error_password_required)
            etPassword.requestFocus()
            return
        }
        if (password.length < 6) {
            tilPassword.error = getString(R.string.error_password_length)
            etPassword.requestFocus()
            return
        }
        if (confirmPassword.isEmpty()) {
            tilConfirmPassword.error = getString(R.string.error_password_required)
            etConfirmPassword.requestFocus()
            return
        }
        if (password != confirmPassword) {
            tilConfirmPassword.error = getString(R.string.error_passwords_mismatch)
            etConfirmPassword.requestFocus()
            return
        }

        // Check if username already exists
        val existingPassword = prefs.getString("user_${username}_password", null)
        if (existingPassword != null) {
            tilUsername.error = getString(R.string.error_user_exists)
            etUsername.requestFocus()
            return
        }

        // Save new user as role = user
        prefs.edit()
            .putString("user_${username}_password", password)
            .putString("user_${username}_role", "user")
            .apply()

        Toast.makeText(this, getString(R.string.registration_success), Toast.LENGTH_LONG).show()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
