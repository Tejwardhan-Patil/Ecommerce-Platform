package com.ecommerce.app.activities

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.ecommerce.app.R
import com.ecommerce.app.models.Product
import com.ecommerce.app.services.ApiService
import com.ecommerce.app.utils.NetworkUtils
import com.ecommerce.app.viewmodels.ProductViewModel
import com.squareup.picasso.Picasso

class ProductDetailActivity : AppCompatActivity() {

    private lateinit var productImageView: ImageView
    private lateinit var productNameTextView: TextView
    private lateinit var productPriceTextView: TextView
    private lateinit var productDescriptionTextView: TextView
    private lateinit var addToCartButton: Button
    private lateinit var buyNowButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var productViewModel: ProductViewModel
    private var productId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        // Initialize Views
        productImageView = findViewById(R.id.product_image_view)
        productNameTextView = findViewById(R.id.product_name_text_view)
        productPriceTextView = findViewById(R.id.product_price_text_view)
        productDescriptionTextView = findViewById(R.id.product_description_text_view)
        addToCartButton = findViewById(R.id.add_to_cart_button)
        buyNowButton = findViewById(R.id.buy_now_button)
        progressBar = findViewById(R.id.progress_bar)

        productViewModel = ViewModelProvider(this).get(ProductViewModel::class.java)

        // Get Product ID from Intent
        productId = intent.getIntExtra("PRODUCT_ID", 0)
        if (productId != 0) {
            loadProductDetails(productId)
        } else {
            Toast.makeText(this, "Product not found!", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Button Click Listeners
        addToCartButton.setOnClickListener {
            addToCart(productId)
        }

        buyNowButton.setOnClickListener {
            initiatePurchase(productId)
        }
    }

    private fun loadProductDetails(productId: Int) {
        if (NetworkUtils.isNetworkAvailable(this)) {
            progressBar.visibility = View.VISIBLE
            productViewModel.getProductDetails(productId).observe(this, Observer { product ->
                progressBar.visibility = View.GONE
                if (product != null) {
                    displayProductDetails(product)
                } else {
                    Toast.makeText(this, "Error loading product details", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayProductDetails(product: Product) {
        Picasso.get().load(product.imageUrl).into(productImageView)
        productNameTextView.text = product.name
        productPriceTextView.text = "Price: $${product.price}"
        productDescriptionTextView.text = product.description
    }

    private fun addToCart(productId: Int) {
        progressBar.visibility = View.VISIBLE
        productViewModel.addToCart(productId).observe(this, Observer { success ->
            progressBar.visibility = View.GONE
            if (success) {
                Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to add to cart", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun initiatePurchase(productId: Int) {
        progressBar.visibility = View.VISIBLE
        productViewModel.purchaseProduct(productId).observe(this, Observer { success ->
            progressBar.visibility = View.GONE
            if (success) {
                Toast.makeText(this, "Purchase successful", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to complete purchase", Toast.LENGTH_SHORT).show()
            }
        })
    }
}