package com.example.agribuddy

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.agribuddy.databinding.ActivityAddProductBinding
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.*
import java.io.ByteArrayOutputStream
import java.io.IOException

class AddProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddProductBinding
    private val firestore = FirebaseFirestore.getInstance()
    private var selectedImageUri: Uri? = null
    private val imgbbApiKey by lazy { getString(R.string.imgbb_api_key) }

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data
            binding.ivProductImagePreview.setImageURI(selectedImageUri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnPickImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            imagePickerLauncher.launch(intent)
        }

        binding.btnAddProduct.setOnClickListener {
            if (selectedImageUri != null) {
                uploadImageToImgBB()
            } else {
                addProductToFirestore(null)
            }
        }
    }

    private fun uploadImageToImgBB() {
        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImageUri)
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val imageBytes = outputStream.toByteArray()
        val encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT)

        val client = OkHttpClient()
        val requestBody = FormBody.Builder()
            .add("key", imgbbApiKey)
            .add("image", encodedImage)
            .build()

        val request = Request.Builder()
            .url("https://api.imgbb.com/1/upload")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@AddProductActivity, "Image upload failed", Toast.LENGTH_SHORT).show()
                    addProductToFirestore(null) // Still upload data without image
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        runOnUiThread {
                            Toast.makeText(this@AddProductActivity, "Image upload failed", Toast.LENGTH_SHORT).show()
                            addProductToFirestore(null)
                        }
                    } else {
                        val responseString = response.body?.string()
                        val imageUrl = Regex("\"url\":\"(.*?)\"").find(responseString ?: "")?.groupValues?.get(1)
                        runOnUiThread {
                            addProductToFirestore(imageUrl)
                        }
                    }
                }
            }
        })
    }

    private fun addProductToFirestore(imageUrl: String?) {
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
            Toast.makeText(this, "Enter a valid price", Toast.LENGTH_SHORT).show()
            return
        }

        val product = hashMapOf(
            "name" to name,
            "price" to price,
            "description" to description,
            "userEmail" to userEmail,
            "imageUrl" to (imageUrl ?: "")
        )

        binding.btnAddProduct.isEnabled = false

        firestore.collection("products")
            .add(product)
            .addOnSuccessListener {
                Toast.makeText(this, "Product added!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to add product", Toast.LENGTH_SHORT).show()
            }
            .addOnCompleteListener {
                binding.btnAddProduct.isEnabled = true
            }
    }
}
