package com.fortyeight.orderformapp


import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @GET("township/api/list")
    suspend fun getTownships(): Response<List<Township>>

    @GET("merchant/list")
    suspend fun getMerchants(): Response<List<Merchant>>

    @POST("saveOrder")
    suspend fun saveOrder(@Body parcel: Parcel): Response<ApiResponse>
    
    // Authentication endpoints
    @POST("api/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>
    
    @POST("register")
    suspend fun register(@Body registerRequest: RegisterRequest): Response<LoginResponse>
    
    @POST("logout")
    suspend fun logout(@Header("Authorization") token: String): Response<ApiResponse>
}