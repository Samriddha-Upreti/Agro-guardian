package com.example.agribuddy

import com.example.agribuddy.model.Product
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.agribuddy.databinding.FragmentMarketplaceBinding
import com.google.firebase.firestore.FirebaseFirestore

class MarketplaceFragment : Fragment() {

    private var _binding: FragmentMarketplaceBinding? = null
    private val binding get() = _binding!!
    private lateinit var productAdapter: ProductAdapter
    private lateinit var db: FirebaseFirestore
    private val productList = mutableListOf<Product>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMarketplaceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()

        setupRecyclerView()
        fetchProductsFromFirebase()

        binding.fabAddProduct.setOnClickListener {
            val intent = Intent(requireContext(), AddProductActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(productList) { product ->
            val intent = Intent(requireContext(), Product_ViewActivity::class.java).apply {
                putExtra("PRODUCT_ID", product.id)
                putExtra("PRODUCT_NAME", product.name)
                putExtra("PRODUCT_PRICE", product.price.toString())
                putExtra("PRODUCT_IMAGE", product.imageUrl)
                putExtra("PRODUCT_DESCRIPTION", product.description ?: "")
                putExtra("SELLER_EMAIL", product.userEmail ?: "")
            }
            startActivity(intent)
        }

        binding.rvProducts.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = productAdapter
            addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    LinearLayoutManager.VERTICAL
                )
            )
        }
    }

    private fun fetchProductsFromFirebase() {
        val currentUserEmail = CurrentUser.email ?: run {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            binding.progressBar.visibility = View.GONE
            showEmptyState()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        Log.d("Marketplace", "Fetching products for user: $currentUserEmail")

        db.collection("products")
            .whereEqualTo("userEmail", currentUserEmail) // Changed to match your Product model field
            .get()
            .addOnSuccessListener { documents ->
                Log.d("Marketplace", "Successfully fetched ${documents.size()} products")

                productList.clear()
                documents.forEach { document ->
                    try {
                        val product = document.toObject(Product::class.java).apply {
                            id = document.id
                        }
                        productList.add(product)
                    } catch (e: Exception) {
                        Log.e("Marketplace", "Error parsing product ${document.id}: ${e.message}")
                    }
                }

                productAdapter.notifyDataSetChanged()
                binding.progressBar.visibility = View.GONE

                if (productList.isEmpty()) {
                    showEmptyState()
                } else {
                    hideEmptyState()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Marketplace", "Error getting products: ${exception.message}")
                binding.progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Failed to load products", Toast.LENGTH_SHORT).show()
                showEmptyState()
            }
    }

    private fun showEmptyState() {
        binding.emptyStateView.visibility = View.VISIBLE
        binding.rvProducts.visibility = View.GONE
    }

    private fun hideEmptyState() {
        binding.emptyStateView.visibility = View.GONE
        binding.rvProducts.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}