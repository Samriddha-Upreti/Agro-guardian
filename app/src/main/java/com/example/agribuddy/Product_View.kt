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

        // Fetch product details from Firestore
        loadProductDetails(productId)

        // Set up the contact button to email the seller
        binding.contactButton.setOnClickListener {
            val email = binding.sellerEmail.text.toString().removePrefix("Contact: ")
            sendEmail(email)
        }
    }

    // Function to load product details from Firestore
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

                    // Update the UI with product details
                    binding.productName.text = name
                    binding.productPrice.text = "Rs. ${price.toInt()}"
                    binding.productDescription.text = description
                    binding.sellerEmail.text = "Contact: $userEmail"

                    // Load product image with Glide
                    Glide.with(this)
                        .load(imageUrl) // This is the URL from Firestore
                        .placeholder(R.drawable.ic_placeholder) // Placeholder image
                        .error(R.drawable.sample_data) // Error image
                        .into(binding.productImage) // Bind image to ImageView
                } else {
                    // Handle case when the product doesn't exist
                    Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener { e ->
                // Handle error when loading product from Firestore
                Toast.makeText(this, "Error loading product", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    // Function to send an email to the seller
    private fun sendEmail(emailAddress: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:") // Email URI
            putExtra(Intent.EXTRA_EMAIL, arrayOf(emailAddress))
            putExtra(Intent.EXTRA_SUBJECT, "Regarding your product listing")
        }
        // Check if an email app is available on the device
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show()
        }
    }
}
