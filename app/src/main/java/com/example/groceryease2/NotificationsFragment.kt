package com.example.groceryease2

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class NotificationFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var spinner: Spinner
    private lateinit var adapter: ProductNotiAdapter
    private var allProducts = mutableListOf<ProductModel>()
    private val db = FirebaseDatabase.getInstance().getReference("products")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_notifications, container, false)

        recyclerView = view.findViewById(R.id.productRecyclerView)
        spinner = view.findViewById(R.id.categoryFilterSpinner)

        setupSpinner()
        setupRecyclerView()
        fetchProducts()

        return view
    }

    private fun setupSpinner() {
        // Updated to match HomeFragment categories
        val categories = arrayOf(
            "All", "Vegetables", "Fruits", "Spices", "Dairy",
            "Oils", "Bakery", "Household", "Pulses", "Beverages", "Snacks"
        )

        val arrayAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, categories)
        spinner.adapter = arrayAdapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                filterProducts(categories[p2])
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
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
        val filtered = if (category == "All") allProducts else allProducts.filter { it.category == category }
        adapter.updateList(filtered)
    }

    private fun showEditDeleteDialog(product: ProductModel, key: String) {
        val builder = AlertDialog.Builder(context)
        val dialogView = LayoutInflater.from(context).inflate(R.layout.activity_add_product, null)
        builder.setView(dialogView)

        val dialog = builder.create()

        val nameInput = dialogView.findViewById<EditText>(R.id.productName)
        val priceInput = dialogView.findViewById<EditText>(R.id.price)
        val qtyInput = dialogView.findViewById<EditText>(R.id.quantity)
        val saveBtn = dialogView.findViewById<Button>(R.id.saveBtn)

        // Pre-fill
        nameInput.setText(product.name)
        priceInput.setText(product.price)
        qtyInput.setText(product.quantity)
        saveBtn.text = "Update"

        saveBtn.setOnClickListener {
            val updatedMap = mapOf(
                "name" to nameInput.text.toString(),
                "price" to priceInput.text.toString(),
                "quantity" to qtyInput.text.toString()
            )
            db.child(key).updateChildren(updatedMap).addOnSuccessListener {
                Toast.makeText(context, "Updated", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }

        // Use neutral button for Delete to avoid layout casting crashes
        dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Delete Product") { _, _ ->
            db.child(key).removeValue().addOnSuccessListener {
                Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }
}