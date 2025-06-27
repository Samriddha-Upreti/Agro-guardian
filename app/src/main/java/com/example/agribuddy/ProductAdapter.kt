package com.example.agribuddy

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.agribuddy.databinding.ItemProductBinding
import com.example.agribuddy.model.Product

class ProductAdapter(private val productList: List<Product>) :
    RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.tvProductName.text = product.name
//            binding.tvProductPrice.text = product.price

            // 🔄 Load image safely from URI string
//            val uri = product.imageUri?.toString()
//            if (!uri.isNullOrEmpty()) {
//                binding.imgProduct.setImageURI(Uri.parse(uri))
//            } else {
//                binding.imgProduct.setImageResource(R.drawable.ic_placeholder)
//            }
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

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(productList[position])
    }

    override fun getItemCount(): Int = productList.size
}