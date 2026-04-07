package com.example.groceryease2



data class CategoryModel(
    var name: String = "",
    var imageResId: Int = 0,
    var imageBase64: String = "",
    var isSelected: Boolean = false
)