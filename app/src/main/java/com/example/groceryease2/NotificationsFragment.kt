package com.example.groceryease2

import android.app.AlertDialog
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.ByteArrayOutputStream
import java.io.OutputStream

class NotificationFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var spinner: Spinner
    private lateinit var adapter: ProductNotiAdapter
    private lateinit var btnExport: Button

    private var allProducts = mutableListOf<ProductModel>()
    private var filteredProducts = mutableListOf<ProductModel>()
    private val categoryList = mutableListOf<String>()

    private val db = FirebaseDatabase.getInstance().getReference("products")

    private var selectedBitmap: Bitmap? = null
    private var currentImageView: ImageView? = null

    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                try {
                    val inputStream = requireActivity().contentResolver.openInputStream(it)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    selectedBitmap = Bitmap.createScaledBitmap(bitmap, 400, 400, false)
                    currentImageView?.setImageBitmap(selectedBitmap)
                } catch (e: Exception) {
                    Toast.makeText(context, "Image load failed", Toast.LENGTH_SHORT).show()
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_notifications, container, false)

        recyclerView = view.findViewById(R.id.productRecyclerView)
        spinner = view.findViewById(R.id.categoryFilterSpinner)
        btnExport = view.findViewById(R.id.btnExportExcel)

        setupRecyclerView()
        setupSpinner()
        fetchProducts()

        btnExport.setOnClickListener {
            exportToExcel()
        }

        return view
    }

    // ✅ DEFAULT + FIREBASE CATEGORIES
    private fun setupSpinner() {

        val ref = FirebaseDatabase.getInstance().getReference("Categories")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                categoryList.clear()

                // ✅ Default categories
                val defaultCategories = listOf(
                    "All", "Vegetables", "Fruits", "Spices", "Dairy",
                    "Oils", "Bakery", "Household", "Pulses", "Beverages", "Snacks"
                )

                categoryList.addAll(defaultCategories)

                // ✅ Firebase categories
                for (data in snapshot.children) {
                    val name = data.child("name").value?.toString()

                    if (!name.isNullOrEmpty() && !categoryList.contains(name)) {
                        categoryList.add(name)
                    }
                }

                val spinnerAdapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_dropdown_item,
                    categoryList
                )

                spinner.adapter = spinnerAdapter

                spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        filterProducts(categoryList[position])
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun setupRecyclerView() {
        adapter = ProductNotiAdapter(allProducts) { product, key ->
            showEditDeleteDialog(product, key)
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun fetchProducts() {
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                allProducts.clear()

                for (data in snapshot.children) {
                    val product = data.getValue(ProductModel::class.java)

                    if (product != null) {
                        val productWithId = product.copy(id = data.key.toString())

                        if (product.id == FirebaseAuth.getInstance().currentUser?.uid) {
                            allProducts.add(productWithId)
                        }
                    }
                }

                filterProducts(spinner.selectedItem?.toString() ?: "All")
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // ✅ FILTER FIX
    private fun filterProducts(category: String) {

        filteredProducts = if (category == "All") {
            allProducts.toMutableList()
        } else {
            allProducts.filter { it.category.equals(category, true) }.toMutableList()
        }

        adapter.updateList(filteredProducts)
    }

    // ✅ FILTERED EXPORT
    private fun exportToExcel() {

        if (filteredProducts.isEmpty()) {
            Toast.makeText(context, "No data to export", Toast.LENGTH_SHORT).show()
            return
        }

        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("GroceryEase Inventory")

        val headers = arrayOf("Product Name", "Category", "Quantity", "Price", "Unit")

        val headerRow = sheet.createRow(0)
        for (i in headers.indices) {
            headerRow.createCell(i).setCellValue(headers[i])
        }

        for (i in filteredProducts.indices) {
            val row = sheet.createRow(i + 1)
            val product = filteredProducts[i]

            row.createCell(0).setCellValue(product.name)
            row.createCell(1).setCellValue(product.category)
            row.createCell(2).setCellValue(product.quantity)
            row.createCell(3).setCellValue(product.price)
            row.createCell(4).setCellValue(product.unit)
        }

        val fileName = "Filtered_Report_${System.currentTimeMillis()}.xlsx"

        try {
            val resolver = requireContext().contentResolver

            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

            uri?.let {
                val outputStream: OutputStream? = resolver.openOutputStream(it)
                workbook.write(outputStream)
                outputStream?.close()
                workbook.close()

                Toast.makeText(context, "Filtered Excel Downloaded", Toast.LENGTH_LONG).show()
            }

        } catch (e: Exception) {
            Toast.makeText(context, "Export Failed", Toast.LENGTH_SHORT).show()
        }
    }

    // ✅ EDIT / DELETE
    private fun showEditDeleteDialog(product: ProductModel, key: String) {

        selectedBitmap = null

        val builder = AlertDialog.Builder(requireContext())
        val dialogView = LayoutInflater.from(context).inflate(R.layout.activity_add_product, null)
        builder.setView(dialogView)
        val dialog = builder.create()

        val nameInput = dialogView.findViewById<EditText>(R.id.productName)
        val priceInput = dialogView.findViewById<EditText>(R.id.price)
        val qtyInput = dialogView.findViewById<EditText>(R.id.quantity)
        val saveBtn = dialogView.findViewById<Button>(R.id.saveBtn)
        val productImage = dialogView.findViewById<ImageView>(R.id.productImage)
        val uploadBtn = dialogView.findViewById<Button>(R.id.uploadBtn)

        currentImageView = productImage

        nameInput.setText(product.name)
        priceInput.setText(product.price)
        qtyInput.setText(product.quantity)
        saveBtn.text = "Update"

        if (!product.image.isNullOrEmpty()) {
            val bytes = Base64.decode(product.image, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            productImage.setImageBitmap(bitmap)
        }

        productImage.setOnClickListener { imagePicker.launch("image/*") }
        uploadBtn.setOnClickListener { imagePicker.launch("image/*") }

        saveBtn.setOnClickListener {

            val map = HashMap<String, Any>()
            map["name"] = nameInput.text.toString()
            map["price"] = priceInput.text.toString()
            map["quantity"] = qtyInput.text.toString()

            val finalBitmap = selectedBitmap ?: (productImage.drawable as BitmapDrawable).bitmap
            map["image"] = imageToBase64(finalBitmap)

            db.child(key).updateChildren(map).addOnSuccessListener {
                Toast.makeText(context, "Updated", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }

        dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Delete Product") { _, _ ->
            db.child(key).removeValue().addOnSuccessListener {
                Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun imageToBase64(bitmap: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos)
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)
    }
}