package com.example.agribuddy

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.agribuddy.LanguageSelectionActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        Handler(Looper.getMainLooper()).postDelayed({
            val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
            val selectedLang = prefs.getString("selected_language", null)

            if (selectedLang == null) {
                // If no language has been selected yet, open LanguageSelectionActivity
                startActivity(Intent(this, LanguageSelectionActivity::class.java))
            } else {
                // If already selected, go to main screen
                startActivity(Intent(this, MainActivity::class.java))
            }
            finish()
        }, 2500)
    }
}
