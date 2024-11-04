package com.ecommerce.app.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import okhttp3.*
import java.io.IOException

object NetworkUtils {

    private const val TAG = "NetworkUtils"
    
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            return when {
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            return activeNetworkInfo != null && activeNetworkInfo.isConnected
        }
    }

    fun makeHttpGetRequest(url: String, callback: (response: String?, error: String?) -> Unit) {
        val client = OkHttpClient()

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "HTTP GET request failed: ${e.message}")
                callback(null, e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    callback(null, "HTTP GET request failed with status code: ${response.code}")
                } else {
                    response.body?.let {
                        callback(it.string(), null)
                    } ?: run {
                        callback(null, "Response body is null")
                    }
                }
            }
        })
    }

    fun makeHttpPostRequest(url: String, jsonBody: String, callback: (response: String?, error: String?) -> Unit) {
        val client = OkHttpClient()

        val mediaType = MediaType.parse("application/json; charset=utf-8")
        val body = RequestBody.create(mediaType, jsonBody)

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "HTTP POST request failed: ${e.message}")
                callback(null, e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    callback(null, "HTTP POST request failed with status code: ${response.code}")
                } else {
                    response.body?.let {
                        callback(it.string(), null)
                    } ?: run {
                        callback(null, "Response body is null")
                    }
                }
            }
        })
    }

    fun makeHttpPutRequest(url: String, jsonBody: String, callback: (response: String?, error: String?) -> Unit) {
        val client = OkHttpClient()

        val mediaType = MediaType.parse("application/json; charset=utf-8")
        val body = RequestBody.create(mediaType, jsonBody)

        val request = Request.Builder()
            .url(url)
            .put(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "HTTP PUT request failed: ${e.message}")
                callback(null, e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    callback(null, "HTTP PUT request failed with status code: ${response.code}")
                } else {
                    response.body?.let {
                        callback(it.string(), null)
                    } ?: run {
                        callback(null, "Response body is null")
                    }
                }
            }
        })
    }

    fun makeHttpDeleteRequest(url: String, callback: (response: String?, error: String?) -> Unit) {
        val client = OkHttpClient()

        val request = Request.Builder()
            .url(url)
            .delete()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "HTTP DELETE request failed: ${e.message}")
                callback(null, e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    callback(null, "HTTP DELETE request failed with status code: ${response.code}")
                } else {
                    response.body?.let {
                        callback(it.string(), null)
                    } ?: run {
                        callback(null, "Response body is null")
                    }
                }
            }
        })
    }

    fun checkInternetSpeed(context: Context): String {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return "No connection"
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return "No connection"
        val downloadSpeed = networkCapabilities.linkDownstreamBandwidthKbps
        val uploadSpeed = networkCapabilities.linkUpstreamBandwidthKbps

        return "Download Speed: ${downloadSpeed / 1000} Mbps, Upload Speed: ${uploadSpeed / 1000} Mbps"
    }

    fun isConnectionFast(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return when {
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                val wifiSpeed = networkCapabilities.linkDownstreamBandwidthKbps
                wifiSpeed >= 25000
            }
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                val cellularSpeed = networkCapabilities.linkDownstreamBandwidthKbps
                cellularSpeed >= 5000
            }
            else -> false
        }
    }

    fun monitorNetworkChanges(context: Context, callback: (isConnected: Boolean) -> Unit) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: android.net.Network) {
                    callback(true)
                }

                override fun onLost(network: android.net.Network) {
                    callback(false)
                }
            })
        } else {
            val activeNetwork = connectivityManager.activeNetworkInfo
            callback(activeNetwork != null && activeNetwork.isConnected)
        }
    }
}