package com.ecommerce.app.services

import android.util.Log
import com.ecommerce.app.models.Product
import com.ecommerce.app.models.User
import com.ecommerce.app.utils.NetworkUtils
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class ApiService {

    private val client = OkHttpClient()

    // Base URL for API requests
    private val baseUrl = "https://api.website.com"

    // API Endpoints
    private val loginEndpoint = "/auth/login"
    private val productsEndpoint = "/products"
    private val ordersEndpoint = "/orders"
    private val profileEndpoint = "/user/profile"

    // Login method
    fun login(username: String, password: String, callback: (Boolean, String) -> Unit) {
        val url = "$baseUrl$loginEndpoint"
        val jsonObject = JSONObject().apply {
            put("username", username)
            put("password", password)
        }

        val body: RequestBody = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            jsonObject.toString()
        )

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ApiService", "Login failed", e)
                callback(false, "Login failed")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        callback(false, "Invalid credentials")
                        return
                    }
                    val responseBody = response.body()?.string()
                    val token = JSONObject(responseBody).getString("token")
                    // Save token to preferences (for future API requests)
                    NetworkUtils.saveToken(token)
                    callback(true, token)
                }
            }
        })
    }

    // Fetch all products
    fun fetchProducts(callback: (Boolean, List<Product>?) -> Unit) {
        val url = "$baseUrl$productsEndpoint"
        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("Authorization", "Bearer ${NetworkUtils.getToken()}")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ApiService", "Fetching products failed", e)
                callback(false, null)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        callback(false, null)
                        return
                    }
                    val responseBody = response.body()?.string()
                    val productArray = JSONArray(responseBody)
                    val productList = mutableListOf<Product>()

                    for (i in 0 until productArray.length()) {
                        val productJson = productArray.getJSONObject(i)
                        val product = Product(
                            id = productJson.getString("id"),
                            name = productJson.getString("name"),
                            price = productJson.getDouble("price"),
                            description = productJson.getString("description"),
                            imageUrl = productJson.getString("image_url")
                        )
                        productList.add(product)
                    }
                    callback(true, productList)
                }
            }
        })
    }

    // Fetch user profile
    fun fetchUserProfile(callback: (Boolean, User?) -> Unit) {
        val url = "$baseUrl$profileEndpoint"
        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("Authorization", "Bearer ${NetworkUtils.getToken()}")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ApiService", "Fetching user profile failed", e)
                callback(false, null)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        callback(false, null)
                        return
                    }
                    val responseBody = response.body()?.string()
                    val userJson = JSONObject(responseBody)
                    val user = User(
                        id = userJson.getString("id"),
                        name = userJson.getString("name"),
                        email = userJson.getString("email")
                    )
                    callback(true, user)
                }
            }
        })
    }

    // Place an order
    fun placeOrder(productId: String, quantity: Int, callback: (Boolean, String) -> Unit) {
        val url = "$baseUrl$ordersEndpoint"
        val jsonObject = JSONObject().apply {
            put("product_id", productId)
            put("quantity", quantity)
        }

        val body: RequestBody = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            jsonObject.toString()
        )

        val request = Request.Builder()
            .url(url)
            .post(body)
            .addHeader("Authorization", "Bearer ${NetworkUtils.getToken()}")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ApiService", "Order placement failed", e)
                callback(false, "Order placement failed")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        callback(false, "Order failed")
                        return
                    }
                    val responseBody = response.body()?.string()
                    callback(true, responseBody ?: "Order placed successfully")
                }
            }
        })
    }

    // Helper method to handle API errors
    private fun handleErrorResponse(response: Response, callback: (Boolean, String) -> Unit) {
        response.use {
            if (!response.isSuccessful) {
                val errorBody = response.body()?.string()
                val errorMessage = JSONObject(errorBody).getString("error_message")
                callback(false, errorMessage)
            }
        }
    }
}