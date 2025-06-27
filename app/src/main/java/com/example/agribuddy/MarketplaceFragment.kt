package com.example.agribuddy

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.agribuddy.adapter.ProductAdapter
import com.example.agribuddy.databinding.DialogAddProductBinding
import com.example.agribuddy.databinding.FragmentMarketplaceBinding
import com.example.agribuddy.model.Product

class MarketplaceFragment : Fragment() {

    private var _binding: FragmentMarketplaceBinding? = null
    private val binding get() = _binding!!

    private lateinit var productAdapter: ProductAdapter
    private var productList = mutableListOf<Product>()
    private var selectedImageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            selectedImageUri = result.data!!.data
            tempDialogBinding?.ivProductImage?.setImageURI(selectedImageUri)
        }
    }

    private var tempDialogBinding: DialogAddProductBinding? = null

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
        setupRecyclerView()
        setupAddProductButton()
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(productList)
        binding.rvMarketplace.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvMarketplace.adapter = productAdapter
    }

    private fun setupAddProductButton() {
        binding.fabAddProduct.setOnClickListener {
            showAddProductDialog()
        }
    }

    private fun showAddProductDialog() {
        val dialogBinding = DialogAddProductBinding.inflate(layoutInflater)
        tempDialogBinding = dialogBinding

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.add_product))
            .setView(dialogBinding.root)
            .setPositiveButton(getString(R.string.btn_add_product)) { _, _ ->
                val name = dialogBinding.etProductName.text.toString()
                val price = dialogBinding.etProductPrice.text.toString()
                val description = dialogBinding.etProductDescription.text.toString()

                if (name.isBlank() || price.isBlank() || selectedImageUri == null) {
                    Toast.makeText(requireContext(), getString(R.string.error_empty_fields), Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val newProduct = Product(name = name, price = price, imageUri = selectedImageUri!!)
                productList.add(newProduct)
                productAdapter.notifyItemInserted(productList.size - 1)
                selectedImageUri = null

            }
            .setNegativeButton(getString(R.string.close), null)
            .create()

        dialogBinding.btnPickImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        tempDialogBinding = null
    }
}
