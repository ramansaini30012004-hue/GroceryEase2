package com.example.groceryease2

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.RecyclerView

class ProductNotiAdapter(
    private var list: List<ProductModel>,
    private val onItemClick: (ProductModel, String) -> Unit // String is the unique ID from Firebase
) : RecyclerView.Adapter<ProductNotiAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.txtProductName)
        val price: TextView = view.findViewById(R.id.txtProductPrice)
        val qty: TextView = view.findViewById(R.id.txtProductQty)
        val image: ImageView = view.findViewById(R.id.imgProduct)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product_noti, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = list[position]
        holder.name.text = product.name
        holder.price.text = "₹${product.price}"
        holder.qty.text = "${product.quantity} ${product.unit}"

        // Decode Base64 to Image
        try {
            val bytes = Base64.decode(product.image, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            holder.image.setImageBitmap(bitmap)
        } catch (e: Exception) {
            holder.image.setImageResource(android.R.drawable.ic_menu_report_image)
        }

        holder.itemView.setOnClickListener {
            // We pass the product to the fragment to show dialog
            onItemClick(product, product.id ?: "")
        }
    }

    override fun getItemCount() = list.size

    fun updateList(newList: List<ProductModel>) {
        list = newList
        notifyDataSetChanged()
    }
}