package com.example.agribuddy

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.agribuddy.databinding.ActivityProductViewBinding
import com.google.firebase.firestore.FirebaseFirestore


class Product_ViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductViewBinding
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()

        // Get product ID from intent
        val productId = intent.getStringExtra("PRODUCT_ID") ?: return finish()

        loadProductDetails(productId)

        binding.contactButton.setOnClickListener {
            val email = binding.sellerEmail.text.toString().removePrefix("Contact: ")
            sendEmail(email)
        }
    }

    private fun loadProductDetails(productId: String) {
        firestore.collection("products").document(productId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Get data from Firestore
                    val name = document.getString("name") ?: ""
                    val price = document.getDouble("price") ?: 0.0
                    val description = document.getString("description") ?: ""
                    val userEmail = document.getString("userEmail") ?: ""
                    val imageUrl = document.getString("imageUrl") ?: ""

                    // Update UI
                    binding.productName.text = name
                    binding.productPrice.text = "Rs. ${price.toInt()}"
                    binding.productDescription.text = description
                    binding.sellerEmail.text = "Contact: $userEmail"

                    // Load image with Glide
                    Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.sample_data)
                        .into(binding.productImage)
                } else {
                    Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading product", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    private fun sendEmail(emailAddress: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(emailAddress))
            putExtra(Intent.EXTRA_SUBJECT, "Regarding your product listing")
        }
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show()
        }
    }
}