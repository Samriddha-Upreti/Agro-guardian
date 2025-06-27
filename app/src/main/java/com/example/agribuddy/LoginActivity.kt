package com.example.agribuddy

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.agribuddy.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.loginButton.setOnClickListener {
            validateLoginForm()
        }

        binding.textRegisterLink.setOnClickListener {
            navigateToRegister()
        }

        binding.textForgotPassword.setOnClickListener {
            // Optional: implement forgot password
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
                loginUser(email, password)
            }
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    CurrentUser.name = user?.displayName
                    CurrentUser.email = user?.email

                    Toast.makeText(this, "Welcome, ${CurrentUser.name ?: "User"}!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(
                        this,
                        "Login failed: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun navigateToRegister() {
        startActivity(Intent(this, RegisterActivity::class.java))
        finish()
    }
}
