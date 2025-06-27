package com.example.agribuddy

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.agribuddy.databinding.ActivityAddProductBinding
import com.google.firebase.firestore.FirebaseFirestore

class AddProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddProductBinding
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnAddProduct.setOnClickListener {
            addProductToFirestore()
        }
    }

    private fun addProductToFirestore() {
        val name = binding.etProductName.text.toString().trim()
        val priceStr = binding.etProductPrice.text.toString().trim()
        val description = binding.etProductDescription.text.toString().trim()
        val userEmail = CurrentUser.email ?: run {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        if (name.isEmpty() || priceStr.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val price = priceStr.toDoubleOrNull() ?: run {
            Toast.makeText(this, "Please enter a valid price", Toast.LENGTH_SHORT).show()
            return
        }

        val product = hashMapOf(
            "name" to name,
            "price" to price,
            "description" to description,
            "userEmail" to userEmail
        )

        firestore.collection("products")
            .add(product)
            .addOnSuccessListener {
                Toast.makeText(this, "Product added successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to add product: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}