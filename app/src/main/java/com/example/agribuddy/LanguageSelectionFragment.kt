package com.example.agribuddy

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class LanguageSelectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_language_selection)

        val btnEnglish = findViewById<Button>(R.id.btn_english)
        val btnNepali = findViewById<Button>(R.id.btn_nepali)

        btnEnglish.setOnClickListener {
            setLocaleAndContinue("en")
        }

        btnNepali.setOnClickListener {
            setLocaleAndContinue("ne")
        }
    }

    private fun setLocaleAndContinue(languageCode: String) {
        // Set the selected language
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        // Save the selected language in SharedPreferences
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        prefs.edit().putString("selected_language", languageCode).apply()

        // Go to registration activity after language is selected
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
        finish() // Finish LanguageSelectionActivity
    }
}
