package com.example.agribuddy

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class LauncherActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get SharedPreferences to check language and registration status
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val language = prefs.getString("selected_language", null) // Get the selected language
        val isRegistered = prefs.getBoolean("is_registered", false) // Check if user is registered

        // Determine which activity to start
        when {
            language == null -> {
                // If language not selected, show the language selection activity
                startActivity(Intent(this, LanguageSelectionActivity::class.java))
            }
            !isRegistered -> {
                // If user has selected a language but not registered, show registration page
                startActivity(Intent(this, RegisterActivity::class.java))
            }
            else -> {
                // If registered, proceed to main activity
                startActivity(Intent(this, MainActivity::class.java))
            }
        }

        // Finish LauncherActivity so user doesn't come back to it
        finish()
    }
}
