package com.example.groceryease2

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class ProductListActivity : AppCompatActivity() {

    lateinit var recyclerView: RecyclerView
    lateinit var title: TextView

    val list = ArrayList<ProductModel>()
    lateinit var adapter: ProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_list)

        recyclerView = findViewById(R.id.productRecycler)
        title = findViewById(R.id.categoryTitle)

        // ✅ CATEGORY FIX
        val category = intent.getStringExtra("category")
            ?.trim()
            ?.replaceFirstChar { it.uppercase() } ?: ""

        title.text = category

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ProductAdapter(list)
        recyclerView.adapter = adapter

        val database = FirebaseDatabase.getInstance().reference

        // ✅ CORRECT PATH + REALTIME LISTENER
        database.child("Products")
            .child(category)
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    list.clear() // 🔥 important

                    for (snap in snapshot.children) {

                        val product = snap.getValue(ProductModel::class.java)

                        if (product != null) {
                            list.add(product)
                        }
                    }

                    adapter.notifyDataSetChanged() // 🔥 refresh UI
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}