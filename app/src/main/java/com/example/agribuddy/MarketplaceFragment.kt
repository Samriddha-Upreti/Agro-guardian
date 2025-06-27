package com.example.agribuddy

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
        setupRecyclerView()


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
        productAdapter = ProductAdapter(productList)
        binding.rvProducts.apply {
            layoutManager = GridLayoutManager(requireContext(), 2) // 2 columns
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
        val userEmail = CurrentUser.email

        if (userEmail.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "User email not found", Toast.LENGTH_SHORT).show()
            showEmptyState()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        Log.d("Marketplace", "Fetching products for user: $userEmail")

        db.collection("products")
            .whereEqualTo("userEmail", userEmail) // Filter products by email
            .get()
            .addOnSuccessListener { documents ->
                Log.d("Marketplace", "Successfully fetched ${documents.size()} products for $userEmail")
                productList.clear()

                for (document in documents) {
                    try {
                        val product = document.toObject(Product::class.java)
                        product.id = document.id
                        productList.add(product)
                        Log.d("Marketplace", "Added product: ${product.name} (ID: ${product.id})")
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