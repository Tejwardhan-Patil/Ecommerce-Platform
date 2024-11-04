package com.ecommerce.app.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ecommerce.app.R
import com.ecommerce.app.models.Product

class ProductAdapter(
    private val context: Context,
    private val productList: List<Product>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(product: Product)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]
        holder.bind(product)
    }

    override fun getItemCount(): Int = productList.size

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private val productImage: ImageView = itemView.findViewById(R.id.productImage)
        private val productName: TextView = itemView.findViewById(R.id.productName)
        private val productPrice: TextView = itemView.findViewById(R.id.productPrice)
        private val productDescription: TextView = itemView.findViewById(R.id.productDescription)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(product: Product) {
            productName.text = product.name
            productPrice.text = context.getString(R.string.product_price, product.price)
            productDescription.text = product.description

            Glide.with(context)
                .load(product.imageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .into(productImage)
        }

        override fun onClick(v: View?) {
            val product = productList[adapterPosition]
            listener.onItemClick(product)
        }
    }
}