package com.example.groceryease2

import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class CategoryAdapter(private val list: MutableList<CategoryModel>) :
    RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.imgCategory)
        val name: TextView = view.findViewById(R.id.txtCategory)
        val add: ImageButton = view.findViewById(R.id.btnAdd)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = list[position]
        val context = holder.itemView.context

        // 🟢 Set Name
        holder.name.text = item.name

        // 🟢 SAFE IMAGE LOADING (MAIN FIX)
        try {
            if (!item.imageBase64.isNullOrEmpty()) {
                val bytes = Base64.decode(item.imageBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                holder.image.setImageBitmap(bitmap)
            } else if (item.imageResId != 0) {
                holder.image.setImageResource(item.imageResId)
            } else {
                holder.image.setImageResource(R.drawable.household)
            }
        } catch (e: Exception) {
            holder.image.setImageResource(R.drawable.household)
        }

        // 🟢 SELECTED UI
        if (item.isSelected) {
            holder.name.setTextColor(
                ContextCompat.getColor(context, android.R.color.black)
            )
            holder.name.setBackgroundColor(
                ContextCompat.getColor(context, R.color.dark_green)
            )
        } else {
            holder.name.setTextColor(
                ContextCompat.getColor(context, android.R.color.black)
            )
            holder.name.setBackgroundColor(
                ContextCompat.getColor(context, android.R.color.transparent)
            )
        }

        // 🟢 ADD BUTTON CLICK
        holder.add.setOnClickListener {
            try {
                val intent = Intent(context, AddProductActivity::class.java)
                intent.putExtra(
                    "category",
                    item.name.trim().replaceFirstChar { it.uppercase() }
                )
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "Error opening Add Product", Toast.LENGTH_SHORT).show()
            }
        }

        // 🟢 ITEM CLICK
        holder.itemView.setOnClickListener {
            try {
                val intent = Intent(context, ProductListActivity::class.java)
                intent.putExtra(
                    "category",
                    item.name.trim().replaceFirstChar { it.uppercase() }
                )
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "Click on button to add Product", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun addCategory(category: CategoryModel) {
        list.add(category)
        notifyItemInserted(list.size - 1)
    }
}