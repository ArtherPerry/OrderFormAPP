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
    @SerializedName("osName") val osName: String
)

data class ApiResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null
)