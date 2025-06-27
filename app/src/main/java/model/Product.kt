package com.example.agribuddy.model

import android.net.Uri

data class Product(
    val name: String,
    val price: String,
    val imageResId: Int? = null,  // for drawable images
    val imageUri: Uri? = null     // for gallery images
)
