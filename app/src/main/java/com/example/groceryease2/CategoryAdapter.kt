package com.example.groceryease2

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

class CategoryAdapter(private val list: MutableList<CategoryModel>) :
    RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    // Executor handles background tasks without needing Coroutine dependencies
    private val executor = Executors.newSingleThreadExecutor()
    private val handler = Handler(Looper.getMainLooper())

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

        // ✅ MANUAL IMAGE LOADING (NO DEPENDENCIES)
        when {
            // 1. Firebase/Web URL (starts with http)
            !item.imageUri.isNullOrEmpty() && item.imageUri!!.startsWith("http") -> {
                holder.image.setImageResource(R.drawable.household) // Placeholder

                executor.execute {
                    val bitmap = downloadBitmap(item.imageUri!!)
                    handler.post {
                        if (bitmap != null) {
                            holder.image.setImageBitmap(bitmap)
                        }
                    }
                }
            }

            // 2. Local Gallery URI (content://)
            !item.imageUri.isNullOrEmpty() -> {
                holder.image.setImageURI(Uri.parse(item.imageUri))
            }

            // 3. Default Resource (R.drawable...)
            item.imageResId != null && item.imageResId != 0 -> {
                holder.image.setImageResource(item.imageResId!!)
            }

            else -> {
                holder.image.setImageResource(R.drawable.household)
            }
        }

        holder.add.setOnClickListener { openAddProductScreen(holder, item) }
        holder.itemView.setOnClickListener { openProductListScreen(holder, item) }
    }

    // Standard Java/Android way to get Bitmap from URL
    private fun downloadBitmap(imageUrl: String): Bitmap? {
        return try {
            val url = URL(imageUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: Exception) {
            e.printStackTrace()
            null
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