package com.example.agribuddy.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.agribuddy.R
import com.example.agribuddy.databinding.ItemProductBinding
import com.example.agribuddy.model.Product

class ProductAdapter(private val productList: List<Product>) :
    RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.tvProductName.text = product.name
            binding.tvProductPrice.text = product.price

            // ✅ Handle both imageResId and imageUri
            when {
                product.imageUri != null -> binding.imgProduct.setImageURI(product.imageUri)
                product.imageResId != null -> binding.imgProduct.setImageResource(product.imageResId)
                else -> binding.imgProduct.setImageResource(R.drawable.ic_placeholder) // fallback
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductViewHolder(binding)
    }

    override fun getItemCount(): Int = productList.size

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(productList[position])
    }
}
