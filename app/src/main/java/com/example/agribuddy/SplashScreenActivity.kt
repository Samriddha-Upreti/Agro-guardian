package com.example.agribuddy

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        Handler(Looper.getMainLooper()).postDelayed({
            val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
            val selectedLang = prefs.getString("selected_language", null)

            // If language is selected, navigate to RegisterActivity
            if (selectedLang != null) {
                startActivity(Intent(this, RegisterActivity::class.java))
            } else {
                // If language is not selected, navigate to LanguageSelectionActivity
                startActivity(Intent(this, LanguageSelectionActivity::class.java))
            }
            finish() // Finish the splash screen activity
        }, 2500) // 2.5 seconds delay
    }
}
