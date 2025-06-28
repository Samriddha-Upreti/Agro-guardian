package com.example.agribuddy

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        Handler(Looper.getMainLooper()).postDelayed({
            // Load user data from SharedPreferences into CurrentUser object
            loadCurrentUserData()

            // Check if user data is saved in CurrentUser object
            if (CurrentUser.name != null && CurrentUser.email != null) {
                // If user is logged in, navigate to MainActivity
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                // Get SharedPreferences to check language selection
                val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
                val language = prefs.getString("selected_language", null) // Get the selected language

                // Determine which activity to start based on language preference
                if (language == null) {
                    // If language not selected, show the language selection activity
                    startActivity(Intent(this, LanguageSelectionActivity::class.java))
                } else {
                    // If language is selected, go to registration screen directly
                    startActivity(Intent(this, RegisterActivity::class.java))
                }
            }

            // Finish SplashActivity so user doesn't come back to it
            finish()
        }, 2500) // 2.5 seconds delay
    }

    private fun loadCurrentUserData() {
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val name = prefs.getString("current_user_name", null)
        val email = prefs.getString("current_user_email", null)

        // Log the loaded data (for debugging)
        Log.d("SplashActivity", "Loaded name: $name, email: $email")

        // If name and email are available, assign them to the CurrentUser object
        if (name != null && email != null) {
            CurrentUser.name = name
            CurrentUser.email = email
        }
    }
}