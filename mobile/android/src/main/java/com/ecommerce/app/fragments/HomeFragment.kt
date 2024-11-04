package com.ecommerce.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ecommerce.app.R
import com.ecommerce.app.adapters.ProductAdapter
import com.ecommerce.app.models.Product
import com.ecommerce.app.services.ApiService
import com.ecommerce.app.utils.NetworkUtils
import com.ecommerce.app.viewmodels.HomeViewModel
import com.ecommerce.app.viewmodels.ProductViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

    private lateinit var productRecyclerView: RecyclerView
    private lateinit var productAdapter: ProductAdapter
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var apiService: ApiService
    private lateinit var progressBar: ProgressBar
    private lateinit var noDataTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        initViews(view)
        setupViewModel()
        observeViewModel()
        loadProducts()
        return view
    }

    private fun initViews(view: View) {
        productRecyclerView = view.findViewById(R.id.product_recycler_view)
        productRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        productAdapter = ProductAdapter(arrayListOf())
        productRecyclerView.adapter = productAdapter

        progressBar = view.findViewById(R.id.progress_bar)
        noDataTextView = view.findViewById(R.id.no_data_text_view)
    }

    private fun setupViewModel() {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        apiService = ApiService()
    }

    private fun observeViewModel() {
        homeViewModel.products.observe(viewLifecycleOwner, Observer { products ->
            products?.let {
                if (it.isEmpty()) {
                    noDataTextView.visibility = View.VISIBLE
                    productRecyclerView.visibility = View.GONE
                } else {
                    noDataTextView.visibility = View.GONE
                    productRecyclerView.visibility = View.VISIBLE
                    productAdapter.updateProducts(it)
                }
            }
        })

        homeViewModel.error.observe(viewLifecycleOwner, Observer { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        })

        homeViewModel.loading.observe(viewLifecycleOwner, Observer { isLoading ->
            if (isLoading) {
                progressBar.visibility = View.VISIBLE
                productRecyclerView.visibility = View.GONE
            } else {
                progressBar.visibility = View.GONE
                productRecyclerView.visibility = View.VISIBLE
            }
        })
    }

    private fun loadProducts() {
        if (NetworkUtils.isNetworkAvailable(requireContext())) {
            homeViewModel.fetchProducts()
        } else {
            Toast.makeText(requireContext(), "No internet connection", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        loadProducts()
    }
}

// Adapter class for product items
class ProductAdapter(private var products: List<Product>) :
    RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        holder.bind(product)
    }

    override fun getItemCount(): Int {
        return products.size
    }

    fun updateProducts(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged()
    }

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val productNameTextView: TextView = itemView.findViewById(R.id.product_name)
        private val productPriceTextView: TextView = itemView.findViewById(R.id.product_price)
        private val productImageView: ImageView = itemView.findViewById(R.id.product_image)
    
        fun bind(product: Product) {
            productNameTextView.text = product.name
            productPriceTextView.text = "${product.price} USD"
    
            // Loading the product image using Glide
            Glide.with(itemView.context)
                .load(product.imageUrl) 
                .placeholder(R.drawable.placeholder_image) 
                .error(R.drawable.error_image) 
                .into(productImageView)
        }
    }    
}

// ViewModel class for HomeFragment
class HomeViewModel : ProductViewModel() {

    val products = mutableLiveData<List<Product>>()
    val error = mutableLiveData<String>()
    val loading = mutableLiveData<Boolean>()

    fun fetchProducts() {
        loading.value = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getProducts()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        products.value = response.body() ?: listOf()
                        loading.value = false
                    } else {
                        error.value = "Failed to load products"
                        loading.value = false
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    error.value = "Error: ${e.message}"
                    loading.value = false
                }
            }
        }
    }
}

// Utility object for network status checks
object NetworkUtils {
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnected
    }
}