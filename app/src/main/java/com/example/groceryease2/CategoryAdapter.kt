package com.example.groceryease2

import android.content.Intent
import android.net.Uri
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

        // ✅ IMAGE HANDLE
        when {
            !item.imageUri.isNullOrEmpty() -> {
                holder.image.setImageURI(Uri.parse(item.imageUri))
            }
            item.imageResId != null && item.imageResId != 0 -> {
                holder.image.setImageResource(item.imageResId!!)
            }
            else -> {
                holder.image.setImageResource(R.drawable.household)
            }
        }

        // ➕ Add Product button (ye AddProduct screen kholega)
        holder.add.setOnClickListener {
            openAddProductScreen(holder, item)
        }

        // 🔥 FULL ITEM CLICK → PRODUCT LIST SHOW
        holder.itemView.setOnClickListener {
            openProductListScreen(holder, item)
        }
    }

    // ✅ ADD PRODUCT SCREEN
    private fun openAddProductScreen(holder: ViewHolder, item: CategoryModel) {
        val context = holder.itemView.context

        val fixedCategory = item.name
            .trim()
            .replaceFirstChar { it.uppercase() }

        val intent = Intent(context, AddProductActivity::class.java)
        intent.putExtra("category", fixedCategory)

        context.startActivity(intent)
    }

    // 🔥 PRODUCT LIST SCREEN (MAIN FIX)
    private fun openProductListScreen(holder: ViewHolder, item: CategoryModel) {
        val context = holder.itemView.context

        val fixedCategory = item.name
            .trim()
            .replaceFirstChar { it.uppercase() }

        val intent = Intent(context, ProductListActivity::class.java) // ✅ IMPORTANT
        intent.putExtra("category", fixedCategory)

        context.startActivity(intent)
    }

    fun addCategory(category: CategoryModel) {
        list.add(category)
        notifyItemInserted(list.size - 1)
    }
}