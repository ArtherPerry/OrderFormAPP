package com.fortyeight.orderformapp

import com.google.gson.annotations.SerializedName

data class Parcel(
    @SerializedName("pickupDate") val pickupDate: String,
    @SerializedName("deliverDate") val deliverDate: String,
    @SerializedName("customerName") val customerName: String,
    @SerializedName("customerPhNumber") val customerPhNumber: String,
    @SerializedName("productAmount") val productAmount: String,
    @SerializedName("extraCharge") val extraCharge: Double = 0.0,
    @SerializedName("osPaidAmount") val osPaidAmount: Double = 0.0,
    @SerializedName("deliveryAddress") val deliveryAddress: String,
    @SerializedName("barcode") val barcode: String,
    @SerializedName("status") val status: String = "PENDING",
    @SerializedName("township") val township: Int,
    @SerializedName("merchant") val merchant: Int,
    @SerializedName("total") val total: Double,
    @SerializedName("parcelSize") val parcelSize: String,
    @SerializedName("deliveryNotes") val deliveryNotes: String = ""
)

data class Township(
    @SerializedName("townshipID") val townshipID: Int,
    @SerializedName("townshipName") val townshipName: String,
    @SerializedName("deliveryCharge") val deliveryCharge: Double
)

data class Merchant(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String
)

data class ApiResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null
)

// Authentication Models
data class LoginRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
)

data class LoginResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("token") val token: String? = null,
    @SerializedName("user") val user: User? = null
)

data class User(
    @SerializedName("id") val id: Long,
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String? = null,
    @SerializedName("roles") val roles: List<String> = emptyList()
)

data class RegisterRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String,
    @SerializedName("email") val email: String? = null
)