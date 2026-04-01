package com.example.groceryease2

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Base64
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import java.io.ByteArrayOutputStream

class NotificationFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var spinner: Spinner
    private lateinit var adapter: ProductNotiAdapter

    private var allProducts = mutableListOf<ProductModel>()
    private val db = FirebaseDatabase.getInstance().getReference("products")

    // 🔥 IMAGE VARIABLES
    private var selectedBitmap: Bitmap? = null
    private var currentImageView: ImageView? = null

    // ✅ NEW IMAGE PICKER (FIXED)
    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                val inputStream = requireActivity().contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)

                selectedBitmap = Bitmap.createScaledBitmap(bitmap, 400, 400, false)
                currentImageView?.setImageBitmap(selectedBitmap)
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

        setupSpinner()
        setupRecyclerView()
        fetchProducts()

        return view
    }

    private fun setupSpinner() {
        val categories = arrayOf(
            "All", "Vegetables", "Fruits", "Spices", "Dairy",
            "Oils", "Bakery", "Household", "Pulses", "Beverages", "Snacks"
        )

        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            categories
        )

        spinner.adapter = spinnerAdapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                filterProducts(categories[position])
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
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
                        allProducts.add(productWithId)
                    }
                }

                filterProducts(spinner.selectedItem.toString())
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun filterProducts(category: String) {
        val filtered = if (category == "All") allProducts
        else allProducts.filter { it.category == category }

        adapter.updateList(filtered)
    }

    // 🔥 MAIN FUNCTION (FULL FIXED)
    private fun showEditDeleteDialog(product: ProductModel, key: String) {

        selectedBitmap = null

        val builder = AlertDialog.Builder(requireContext())
        val dialogView = LayoutInflater.from(context)
            .inflate(R.layout.activity_add_product, null)
        builder.setView(dialogView)

        val dialog = builder.create()

        val nameInput = dialogView.findViewById<EditText>(R.id.productName)
        val priceInput = dialogView.findViewById<EditText>(R.id.price)
        val qtyInput = dialogView.findViewById<EditText>(R.id.quantity)
        val saveBtn = dialogView.findViewById<Button>(R.id.saveBtn)
        val productImage = dialogView.findViewById<ImageView>(R.id.productImage)

        currentImageView = productImage

        // Prefill
        nameInput.setText(product.name)
        priceInput.setText(product.price)
        qtyInput.setText(product.quantity)
        saveBtn.text = "Update"

        // Load old image
        if (!product.image.isNullOrEmpty()) {
            val bytes = Base64.decode(product.image, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            productImage.setImageBitmap(bitmap)
        }

        // Change image
        productImage.setOnClickListener {
            imagePicker.launch("image/*")
        }

        // Save
        saveBtn.setOnClickListener {

            val map = HashMap<String, Any>()
            map["name"] = nameInput.text.toString()
            map["price"] = priceInput.text.toString()
            map["quantity"] = qtyInput.text.toString()

            val finalBitmap = selectedBitmap ?: run {
                val drawable = productImage.drawable
                val bitmap = (drawable as BitmapDrawable).bitmap
                bitmap
            }

            map["image"] = imageToBase64(finalBitmap)

            db.child(key).updateChildren(map).addOnSuccessListener {
                Toast.makeText(context, "Updated", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }

        // Delete
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