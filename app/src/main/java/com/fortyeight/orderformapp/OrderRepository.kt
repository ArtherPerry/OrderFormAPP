package com.fortyeight.orderformapp // Your package

// Assuming these imports are correct and your data classes are defined in Models.kt
// import com.fortyeight.orderformapp.Models.ApiResponse
// import com.fortyeight.orderformapp.Models.Merchant
// import com.fortyeight.orderformapp.Models.Parcel
// import com.fortyeight.orderformapp.Models.Township
// No, your Models.kt defines them directly in com.fortyeight.orderformapp

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import android.util.Log // Added for logging

class OrderRepository {
    private var apiService: ApiService? = null

    // This is where Retrofit client and ApiService are configured
    fun setBaseUrl(baseUrl: String) {
        Log.d("OrderRepository", "Setting base URL to: $baseUrl") // Log the URL being set

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // Good, this will log request/response bodies
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        // Ensure the baseUrl ends with a slash if it doesn't already
        val sanitizedBaseUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        Log.d("OrderRepository", "Sanitized base URL for Retrofit: $sanitizedBaseUrl")


        try {
            val retrofit = Retrofit.Builder()
                .baseUrl(sanitizedBaseUrl) // Uses the sanitized URL
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            apiService = retrofit.create(ApiService::class.java)
            Log.i("OrderRepository", "Retrofit ApiService initialized successfully with base URL: $sanitizedBaseUrl")
        } catch (e: IllegalArgumentException) {
            Log.e("OrderRepository", "Error initializing Retrofit: Invalid base URL? '$sanitizedBaseUrl'", e)
            apiService = null // Ensure apiService is null if Retrofit setup fails
        } catch (e: Exception) {
            Log.e("OrderRepository", "Generic error initializing Retrofit with URL '$sanitizedBaseUrl'", e)
            apiService = null
        }
    }

    suspend fun getTownships(): Result<List<Township>> {
        val currentApiService = apiService // Capture the current state of apiService
        if (currentApiService == null) {
            Log.e("OrderRepository", "getTownships called but ApiService is null. Was setBaseUrl called with a valid URL?")
            return Result.failure(Exception("API service not initialized. Call setBaseUrl first."))
        }

        return try {
            Log.d("OrderRepository", "Attempting to fetch townships via Retrofit...")
            val response = currentApiService.getTownships() // THE ACTUAL RETROFIT CALL
            Log.d("OrderRepository", "Townships API Response - Code: ${response.code()}, Successful: ${response.isSuccessful}")

            if (response.isSuccessful) {
                val townshipList = response.body()
                if (townshipList != null) {
                    Log.i("OrderRepository", "Townships fetched successfully. Count: ${townshipList.size}")
                    if (townshipList.isEmpty()) {
                        Log.w("OrderRepository", "Server returned an empty list of townships.")
                    } else {
                        townshipList.forEachIndexed { index, township ->
                            Log.d("OrderRepository", "Township $index: ID=${township.townshipID}, Name=${township.townshipName}, Charge=${township.deliveryCharge}")
                        }
                    }
                    Result.success(townshipList)
                } else {
                    Log.e("OrderRepository", "Townships response body is NULL even though response was successful (Code: ${response.code()}).")
                    Result.failure(Exception("Failed to fetch townships: Response body is null"))
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "No error body"
                Log.e("OrderRepository", "Failed to fetch townships. Code: ${response.code()}, Message: ${response.message()}. Error Body: $errorBody")
                Result.failure(Exception("Failed to fetch townships: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("OrderRepository", "Exception during getTownships API call: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getMerchants(): Result<List<Merchant>> {
        val currentApiService = apiService
        if (currentApiService == null) {
            Log.e("OrderRepository", "getMerchants called but ApiService is null.")
            return Result.failure(Exception("API service not initialized"))
        }
        return try {
            Log.d("OrderRepository", "Attempting to fetch merchants...")
            val response = currentApiService.getMerchants()
            Log.d("OrderRepository", "Merchants API Response - Code: ${response.code()}, Successful: ${response.isSuccessful}")
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                val errorBody = response.errorBody()?.string() ?: "No error body"
                Log.e("OrderRepository", "Failed to fetch merchants. Code: ${response.code()}, Error: $errorBody")
                Result.failure(Exception("Failed to fetch merchants"))
            }
        } catch (e: Exception) {
            Log.e("OrderRepository", "Exception during getMerchants API call: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun saveOrder(parcel: Parcel): Result<ApiResponse> {
        val currentApiService = apiService
        if (currentApiService == null) {
            Log.e("OrderRepository", "saveOrder called but ApiService is null.")
            return Result.failure(Exception("API service not initialized"))
        }
        return try {
            Log.d("OrderRepository", "Attempting to save order...")
            val response = currentApiService.saveOrder(parcel)
            Log.d("OrderRepository", "SaveOrder API Response - Code: ${response.code()}, Successful: ${response.isSuccessful}")
            if (response.isSuccessful) {
                Result.success(response.body() ?: ApiResponse(false, "Unknown error on successful save"))
            } else {
                val errorBody = response.errorBody()?.string() ?: "No error body"
                Log.e("OrderRepository", "Failed to save order. Code: ${response.code()}, Error: $errorBody")
                Result.failure(Exception("Failed to save order"))
            }
        } catch (e: Exception) {
            Log.e("OrderRepository", "Exception during saveOrder API call: ${e.message}", e)
            Result.failure(e)
        }
    }
}

