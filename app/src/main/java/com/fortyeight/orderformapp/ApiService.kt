package com.fortyeight.orderformapp


import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @GET("township/api/list")
    suspend fun getTownships(): Response<List<Township>>

    @GET("merchant")
    suspend fun getMerchants(): Response<List<Merchant>>

    @POST("saveOrder")
    suspend fun saveOrder(@Body parcel: Parcel): Response<ApiResponse>
}