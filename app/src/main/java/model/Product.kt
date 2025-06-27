package com.example.agribuddy.model

data class Product(
    var id: String = "",
    val name: String = "",
    val price: Any = "",
    val imageUrl: String = "",
    val description: String? = null,  // Made nullable
    val userEmail: String? = null     // Made nullable
) {
    fun getPriceString(): String {
        return when (price) {
            is Double -> "Rs. ${"%.2f".format(price)}"
            is String -> price
            else -> price.toString()
        }
    }
}