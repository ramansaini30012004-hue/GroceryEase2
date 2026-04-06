package com.example.groceryease2



data class CategoryModel(
    var name: String = "",
    var imageBase64: String? = null,   // 🔥 Base64 image
    var imageResId: Int? = null,
    var userId: String? = null
)