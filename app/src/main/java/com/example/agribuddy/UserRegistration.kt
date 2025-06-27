package com.example.agribuddy

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.agribuddy.databinding.ActivityUserRegistrationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserRegistrationBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        binding.registerButton.setOnClickListener {
            registerUser()
        }

        binding.textLoginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun registerUser() {
        val name = binding.editName.text.toString().trim()
        val email = binding.editEmail.text.toString().trim()
        val password = binding.editPassword.text.toString()
        val confirmPassword = binding.editConfirmPassword.text.toString()

        when {
            name.isEmpty() -> {
                binding.editName.error = "Full name is required"
                binding.editName.requestFocus()
                return
            }
            email.isEmpty() -> {
                binding.editEmail.error = "Email is required"
                binding.editEmail.requestFocus()
                return
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.editEmail.error = "Please enter a valid email"
                binding.editEmail.requestFocus()
                return
            }
            password.isEmpty() -> {
                binding.editPassword.error = "Password is required"
                binding.editPassword.requestFocus()
                return
            }
            password.length < 6 -> {
                binding.editPassword.error = "Password must be at least 6 characters"
                binding.editPassword.requestFocus()
                return
            }
            confirmPassword.isEmpty() -> {
                binding.editConfirmPassword.error = "Please confirm your password"
                binding.editConfirmPassword.requestFocus()
                return
            }
            password != confirmPassword -> {
                binding.editConfirmPassword.error = "Passwords do not match"
                binding.editConfirmPassword.requestFocus()
                return
            }
            else -> {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Set display name
                            val user = auth.currentUser
                            val profileUpdates = UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .build()
                            user?.updateProfile(profileUpdates)

                            Toast.makeText(
                                this,
                                "Registration successful!",
                                Toast.LENGTH_SHORT
                            ).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(
                                this,
                                "Registration failed: ${task.exception?.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
            }
        }
    }
}
