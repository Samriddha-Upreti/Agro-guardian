package com.example.agribuddy

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import com.example.agribuddy.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginButton.setOnClickListener {
            validateLoginForm()
        }

        binding.textRegisterLink.setOnClickListener {
            navigateToRegister()
        }

        binding.textForgotPassword.setOnClickListener {

        }
    }

    private fun validateLoginForm() {
        val email = binding.editLoginEmail.text.toString().trim()
        val password = binding.editLoginPassword.text.toString()

        when {
            email.isEmpty() -> {
                binding.editLoginEmail.error = "Email is required"
                binding.editLoginEmail.requestFocus()
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.editLoginEmail.error = "Please enter a valid email"
                binding.editLoginEmail.requestFocus()
            }
            password.isEmpty() -> {
                binding.editLoginPassword.error = "Password is required"
                binding.editLoginPassword.requestFocus()
            }
            else -> {

                showLoginSuccess()
            }
        }
    }

    private fun showLoginSuccess() {
        // Replace this with your actual success handling
        binding.editLoginEmail.error = null
        binding.editLoginPassword.error = null
        // Toast.makeText(this, "Login validation successful", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToRegister() {
        startActivity(Intent(this, RegisterActivity::class.java))
        finish()
    }


}