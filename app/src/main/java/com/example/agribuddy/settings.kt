package com.example.agribuddy.ui.settings

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.agribuddy.LocaleHelper
import com.example.agribuddy.MainActivity
import com.example.agribuddy.R
import com.example.agribuddy.databinding.FragmentSettingsBinding
import java.util.*

class settings : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onAttach(context: Context) {
        super.onAttach(LocaleHelper.applyLocale(context))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        // Show current language
        val currentLang = Locale.getDefault().language
        binding.tvSelectedLanguage.text = if (currentLang == "ne") "नेपाली" else "English"

        // Handle language change
        binding.languageSettingContainer.setOnClickListener {
            showLanguageDialog()
        }

        return binding.root
    }

    private fun showLanguageDialog() {
        val languages = arrayOf("English", "नेपाली")
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.change_language))
            .setItems(languages) { _, which ->
                when (which) {
                    0 -> setLocale("en")
                    1 -> setLocale("ne")
                }
            }
            .create()
            .show()
    }

    private fun setLocale(languageCode: String) {
        val prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("selected_language", languageCode).apply()

        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
