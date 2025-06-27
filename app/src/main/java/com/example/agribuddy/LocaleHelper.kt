package com.example.agribuddy

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.*

object LocaleHelper {

    // Apply saved language from SharedPreferences
    fun applyLocale(context: Context): Context {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val langCode = prefs.getString("selected_language", "en") ?: "en"
        return updateLocale(context, langCode)
    }

    // Apply given language code
    fun updateLocale(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            context
        }
    }
}
