package com.ecommerce.app.services

import android.content.Context
import android.content.SharedPreferences
import com.ecommerce.app.models.User
import com.ecommerce.app.utils.NetworkUtils
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class AuthService(private val context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE)
    private val okHttpClient: OkHttpClient = OkHttpClient()

    companion object {
        const val BASE_URL = "https://website.com/api/auth"
        const val TOKEN_KEY = "auth_token"
        const val USER_ID_KEY = "user_id"
    }

    // User login function
    fun login(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            callback(false, "No network connection")
            return
        }

        val url = "$BASE_URL/login"
        val json = JSONObject().apply {
            put("email", email)
            put("password", password)
        }

        val body = RequestBody.create(MediaType.parse("application/json"), json.toString())
        val request = Request.Builder().url(url).post(body).build()

        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body()?.string()
                    responseBody?.let {
                        val jsonResponse = JSONObject(it)
                        val token = jsonResponse.getString("token")
                        val userId = jsonResponse.getString("user_id")

                        // Save the token and user ID
                        saveAuthToken(token)
                        saveUserId(userId)

                        callback(true, null)
                    } ?: callback(false, "Empty response")
                } else {
                    callback(false, "Authentication failed")
                }
            }
        })
    }

    // User logout function
    fun logout(callback: (Boolean) -> Unit) {
        if (!isLoggedIn()) {
            callback(false)
            return
        }

        val url = "$BASE_URL/logout"
        val token = getAuthToken()
        val request = Request.Builder().url(url).addHeader("Authorization", "Bearer $token").post(RequestBody.create(null, "")).build()

        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    clearSession()
                    callback(true)
                } else {
                    callback(false)
                }
            }
        })
    }

    // Register new user
    fun register(user: User, callback: (Boolean, String?) -> Unit) {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            callback(false, "No network connection")
            return
        }

        val url = "$BASE_URL/register"
        val json = JSONObject().apply {
            put("name", user.name)
            put("email", user.email)
            put("password", user.password)
        }

        val body = RequestBody.create(MediaType.parse("application/json"), json.toString())
        val request = Request.Builder().url(url).post(body).build()

        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body()?.string()
                    responseBody?.let {
                        val jsonResponse = JSONObject(it)
                        val token = jsonResponse.getString("token")
                        val userId = jsonResponse.getString("user_id")

                        // Save the token and user ID
                        saveAuthToken(token)
                        saveUserId(userId)

                        callback(true, null)
                    } ?: callback(false, "Empty response")
                } else {
                    callback(false, "Registration failed")
                }
            }
        })
    }

    // Check if user is logged in
    fun isLoggedIn(): Boolean {
        return !getAuthToken().isNullOrEmpty()
    }

    // Get user authentication token
    private fun getAuthToken(): String? {
        return sharedPreferences.getString(TOKEN_KEY, null)
    }

    // Save user authentication token
    private fun saveAuthToken(token: String) {
        sharedPreferences.edit().putString(TOKEN_KEY, token).apply()
    }

    // Get user ID
    private fun getUserId(): String? {
        return sharedPreferences.getString(USER_ID_KEY, null)
    }

    // Save user ID
    private fun saveUserId(userId: String) {
        sharedPreferences.edit().putString(USER_ID_KEY, userId).apply()
    }

    // Clear authentication session
    private fun clearSession() {
        sharedPreferences.edit().clear().apply()
    }

    // Retrieve user profile details
    fun getUserProfile(callback: (User?) -> Unit) {
        val userId = getUserId() ?: run {
            callback(null)
            return
        }

        val url = "$BASE_URL/user/$userId"
        val token = getAuthToken()

        val request = Request.Builder().url(url).addHeader("Authorization", "Bearer $token").get().build()

        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body()?.string()
                    responseBody?.let {
                        val jsonResponse = JSONObject(it)
                        val user = User(
                            id = jsonResponse.getString("id"),
                            name = jsonResponse.getString("name"),
                            email = jsonResponse.getString("email")
                        )
                        callback(user)
                    } ?: callback(null)
                } else {
                    callback(null)
                }
            }
        })
    }
}