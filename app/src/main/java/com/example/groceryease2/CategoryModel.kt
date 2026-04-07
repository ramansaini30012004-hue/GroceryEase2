package com.example.groceryease2



data class CategoryModel(
    var name: String = "",
    var imageBase64: String? = null,
    var imageResId: Int? = null,
    var userId: String? = null,
    var isSelected: Boolean = false   // ⭐ ADD THIS
)