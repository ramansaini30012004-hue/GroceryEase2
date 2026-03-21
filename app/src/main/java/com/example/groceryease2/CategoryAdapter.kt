package com.example.groceryease2

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CategoryAdapter(private val list: List<CategoryModel>) :
    RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val image = view.findViewById<ImageView>(R.id.imgCategory)
        val name = view.findViewById<TextView>(R.id.txtCategory)
        val add = view.findViewById<ImageButton>(R.id.btnAdd)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = list[position]

        holder.name.text = item.name
        holder.image.setImageResource(item.image)

        // ➕ PLUS BUTTON CLICK
        holder.add.setOnClickListener {

            val context = holder.itemView.context

            val intent = Intent(context, AddProductActivity::class.java)

            // 🔥 CATEGORY NAME SEND
            intent.putExtra("category", item.name)

            context.startActivity(intent)
        }
    }
}