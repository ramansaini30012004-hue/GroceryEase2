package com.example.groceryease2

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase

class ProductListActivity : AppCompatActivity() {

    lateinit var recyclerView: RecyclerView
    lateinit var title:TextView

    val list = ArrayList<ProductModel>()
    lateinit var adapter:ProductAdapter

    override fun onCreate(savedInstanceState:Bundle?){

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_list)

        recyclerView = findViewById(R.id.productRecycler)
        title = findViewById(R.id.categoryTitle)

        val category = intent.getStringExtra("category")

        title.text = category

        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = ProductAdapter(list)

        recyclerView.adapter = adapter

        val database = FirebaseDatabase.getInstance().reference

        database.child("products")
            .child(category!!)
            .get()
            .addOnSuccessListener {

                for(snapshot in it.children){

                    val product = snapshot.getValue(ProductModel::class.java)

                    if(product!=null){

                        list.add(product)
                    }
                }

                adapter.notifyDataSetChanged()
            }
    }
}