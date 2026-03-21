package com.example.groceryease2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ProductAdapter(private val list:List<ProductModel>) :
    RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    class ViewHolder(view:View):RecyclerView.ViewHolder(view){

        val name = view.findViewById<TextView>(R.id.productName)
        val price = view.findViewById<TextView>(R.id.productPrice)
        val image = view.findViewById<ImageView>(R.id.productImg)
    }

    override fun onCreateViewHolder(parent:ViewGroup,viewType:Int):ViewHolder{

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product,parent,false)

        return ViewHolder(view)
    }

    override fun getItemCount():Int{

        return list.size
    }

    override fun onBindViewHolder(holder:ViewHolder,position:Int){

        val item = list[position]

        holder.name.text = item.name
        holder.price.text = "₹ "+item.price
    }
}