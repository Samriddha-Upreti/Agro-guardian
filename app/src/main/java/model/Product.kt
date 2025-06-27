package com.example.agribuddy
data class Product(
    var id: String = "",
    val name: String = "",
    val price: Any = "", // Changed from String to Any
    val imageUrl: String = ""
) {
    // Helper function to get price as String
    fun getPriceString(): String {
        return when (price) {
            is Double -> "Rs. ${"%.2f".format(price)}" // Formats to $2.99
            is String -> price
            else -> price.toString()
        }
    }
}