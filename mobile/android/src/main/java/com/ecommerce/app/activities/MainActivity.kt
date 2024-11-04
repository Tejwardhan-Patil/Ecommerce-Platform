package com.ecommerce.app.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ecommerce.app.R
import com.ecommerce.app.adapters.ProductAdapter
import com.ecommerce.app.databinding.ActivityMainBinding
import com.ecommerce.app.models.Product
import com.ecommerce.app.services.ApiService
import com.ecommerce.app.utils.NetworkUtils
import com.ecommerce.app.viewmodels.ProductViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var productAdapter: ProductAdapter
    private lateinit var productViewModel: ProductViewModel
    private val apiService = ApiService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupViewModel()
        setupListeners()
        fetchProducts()

        binding.swipeRefreshLayout.setOnRefreshListener {
            fetchProducts()
        }
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(this, listOf())
        binding.recyclerViewProducts.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewProducts.adapter = productAdapter
    }

    private fun setupViewModel() {
        productViewModel = ViewModelProvider(this).get(ProductViewModel::class.java)
        productViewModel.products.observe(this, { products ->
            updateProductList(products)
        })
    }

    private fun setupListeners() {
        binding.btnViewCart.setOnClickListener {
            navigateToCart()
        }

        binding.btnProfile.setOnClickListener {
            navigateToProfile()
        }
    }

    private fun navigateToCart() {
        val intent = Intent(this, CartActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToProfile() {
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
    }

    private fun updateProductList(products: List<Product>) {
        productAdapter.updateProducts(products)
        binding.swipeRefreshLayout.isRefreshing = false
    }

    private fun fetchProducts() {
        if (NetworkUtils.isNetworkAvailable(this)) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val products = apiService.getProducts()
                    withContext(Dispatchers.Main) {
                        productViewModel.setProducts(products)
                    }
                } catch (e: Exception) {
                    handleErrorFetchingProducts()
                }
            }
        } else {
            displayNoNetworkToast()
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun handleErrorFetchingProducts() {
        runOnUiThread {
            Toast.makeText(
                this@MainActivity,
                "Failed to load products. Please try again later.",
                Toast.LENGTH_SHORT
            ).show()
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun displayNoNetworkToast() {
        Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_cart -> {
                navigateToCart()
                true
            }
            R.id.action_profile -> {
                navigateToProfile()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        fetchProducts()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}