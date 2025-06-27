package com.example.agribuddy.model

data class Product(
    val name: String,
    val price: Double,
    val description: String,
    val imageUrl: String = ""
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "name" to name,
            "price" to price,
            "description" to description,
            "imageUrl" to imageUrl
        )
    }
}