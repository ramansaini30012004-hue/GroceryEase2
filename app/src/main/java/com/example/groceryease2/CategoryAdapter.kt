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

        holder.name.text = item.name

        // ✅ BASE64 IMAGE LOGIC
        if (!item.imageBase64.isNullOrEmpty()) {
            try {
                val bytes = Base64.decode(item.imageBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                holder.image.setImageBitmap(bitmap)
            } catch (e: Exception) {
                holder.image.setImageResource(R.drawable.household)
            }

        } else if (item.imageResId != null && item.imageResId != 0) {
            // ✅ Default drawable
            holder.image.setImageResource(item.imageResId!!)
        } else {
            // ✅ Fallback
            holder.image.setImageResource(R.drawable.household)
        }

        holder.add.setOnClickListener {
            openAddProductScreen(holder, item)
        }

        holder.itemView.setOnClickListener {
            openProductListScreen(holder, item)
        }
    }

    private fun openAddProductScreen(holder: ViewHolder, item: CategoryModel) {
        val context = holder.itemView.context
        val intent = Intent(context, AddProductActivity::class.java).apply {
            putExtra("category", item.name.trim().replaceFirstChar { it.uppercase() })
        }
        context.startActivity(intent)
    }

    private fun openProductListScreen(holder: ViewHolder, item: CategoryModel) {
        val context = holder.itemView.context
        val intent = Intent(context, ProductListActivity::class.java).apply {
            putExtra("category", item.name.trim().replaceFirstChar { it.uppercase() })
        }
        context.startActivity(intent)
    }

    fun addCategory(category: CategoryModel) {
        list.add(category)
        notifyItemInserted(list.size - 1)
    }
}