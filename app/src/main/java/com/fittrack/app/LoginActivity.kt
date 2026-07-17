package com.fittrack.app

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class LoginActivity : AppCompatActivity() {

    private lateinit var tilUsername: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var etUsername: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: MaterialButton
    private lateinit var tvGoToRegister: android.widget.TextView
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        prefs = getSharedPreferences("FitTrackPrefs", MODE_PRIVATE)
        seedDemoAccounts()

        tilUsername = findViewById(R.id.tilLoginUsername)
        tilPassword = findViewById(R.id.tilLoginPassword)
        etUsername = findViewById(R.id.etLoginUsername)
        etPassword = findViewById(R.id.etLoginPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvGoToRegister = findViewById(R.id.tvGoToRegister)

        btnLogin.setOnClickListener { loginUser() }
        tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }

    /**
     * Seeds two built-in demo accounts on first launch:
     *   admin / admin123  (role = admin)
     *   john  / user123   (role = user)
     */
    private fun seedDemoAccounts() {
        val seeded = prefs.getBoolean("demo_seeded", false)
        if (!seeded) {
            prefs.edit()
                // Admin account
                .putString("user_admin_password", "admin123")
                .putString("user_admin_role", "admin")
                // Demo user account
                .putString("user_john_password", "user123")
                .putString("user_john_role", "user")
                .putBoolean("demo_seeded", true)
                .apply()
        }
    }

    private fun loginUser() {
        tilUsername.error = null
        tilPassword.error = null

        val username = etUsername.text.toString().trim().lowercase()
        val password = etPassword.text.toString().trim()

        if (username.isEmpty()) {
            tilUsername.error = getString(R.string.error_username_required)
            etUsername.requestFocus()
            return
        }
        if (password.isEmpty()) {
            tilPassword.error = getString(R.string.error_password_required)
            etPassword.requestFocus()
            return
        }

        val savedPassword = prefs.getString("user_${username}_password", null)
        val savedRole = prefs.getString("user_${username}_role", "user")

        if (savedPassword != null && savedPassword == password) {
            prefs.edit()
                .putBoolean("isLoggedIn", true)
                .putString("loggedInUser", username)
                .putString("loggedInRole", savedRole)
                .apply()

            Toast.makeText(this, getString(R.string.login_success), Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        } else {
            Toast.makeText(this, getString(R.string.error_invalid_credentials), Toast.LENGTH_LONG).show()
            tilPassword.error = "Check your password"
        }
    }
}
