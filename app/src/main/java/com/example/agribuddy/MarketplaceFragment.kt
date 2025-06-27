package com.example.agribuddy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.agribuddy.adapter.ProductAdapter
import com.example.agribuddy.databinding.FragmentMarketplaceBinding
import com.example.agribuddy.model.Product

class MarketplaceFragment : Fragment() {

    private var _binding: FragmentMarketplaceBinding? = null
    private val binding get() = _binding!!

    private lateinit var productAdapter: ProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMarketplaceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sampleProducts = listOf(
            Product("Wheat Seeds", "Rs. 1500", R.drawable.sample_data),
            Product("Tractor Blade", "Rs. 7500", R.drawable.sample_data),
            Product("Organic Fertilizer", "Rs. 1200", R.drawable.sample_data),
            Product("Rice Seeds", "Rs. 1350", R.drawable.sample_data)
        )

        productAdapter = ProductAdapter(sampleProducts)

        binding.rvMarketplace.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvMarketplace.adapter = productAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
