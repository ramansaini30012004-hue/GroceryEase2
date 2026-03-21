package com.example.groceryease2

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream
import java.io.InputStream

class AddProductActivity : AppCompatActivity() {

    lateinit var productImage: ImageView
    lateinit var uploadBtn: Button
    lateinit var saveBtn: Button

    lateinit var productName: EditText
    lateinit var quantity: EditText
    lateinit var price: EditText
    lateinit var unitSpinner: Spinner

    var imageBase64: String? = null

    val PICK_IMAGE = 1
    var category: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_product)

        // ✅ STATUS BAR COLOR CHANGE
        window.statusBarColor = getColor(R.color.button_green)

        // ✅ Status bar icons (white)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = 0
        }
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_product)

        productImage = findViewById(R.id.productImage)
        uploadBtn = findViewById(R.id.uploadBtn)
        saveBtn = findViewById(R.id.saveBtn)

        productName = findViewById(R.id.productName)
        quantity = findViewById(R.id.quantity)
        price = findViewById(R.id.price)
        unitSpinner = findViewById(R.id.unitSpinner)

        category = intent.getStringExtra("category")

        val units = arrayOf("Kg", "Packet", "Liter", "Piece")

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            units
        )

        unitSpinner.adapter = adapter

        // Select Image
        uploadBtn.setOnClickListener {

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"

            startActivityForResult(intent, PICK_IMAGE)
        }

        // Save Product
        saveBtn.setOnClickListener {

            val name = productName.text.toString()
            val qty = quantity.text.toString()
            val unit = unitSpinner.selectedItem.toString()
            val priceValue = price.text.toString()

            if (name.isEmpty() || qty.isEmpty() || priceValue.isEmpty() || imageBase64 == null) {

                Toast.makeText(this, "Fill all fields & select image", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val database = FirebaseDatabase.getInstance().getReference("products")

            val productId = database.push().key!!

            val product = ProductModel(
                id = FirebaseAuth.getInstance().uid.toString(),
                name,
                qty,
                unit,
                priceValue,
                imageBase64!!,
                category = category.toString()
            )

            category?.let {

                database
                    .child(productId)
                    .setValue(product)
                    .addOnSuccessListener {

                        Toast.makeText(this, "Product successfully added", Toast.LENGTH_LONG).show()

                        clearFields()
                    }
                    .addOnFailureListener {

                        Toast.makeText(this, "Failed to save product", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {

            val imageUri: Uri? = data?.data

            productImage.setImageURI(imageUri)

            imageUri?.let {

                val inputStream: InputStream? = contentResolver.openInputStream(it)

                val bitmap = BitmapFactory.decodeStream(inputStream)

                imageBase64 = encodeImage(bitmap)
            }
        }
    }

    private fun encodeImage(bitmap: Bitmap): String {

        val byteArrayOutputStream = ByteArrayOutputStream()

        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, byteArrayOutputStream)

        val bytes = byteArrayOutputStream.toByteArray()

        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    private fun clearFields() {

        productName.text.clear()
        quantity.text.clear()
        price.text.clear()

        productImage.setImageResource(android.R.drawable.ic_menu_camera)

        imageBase64 = null
    }
}