package com.example.climo.data.remote

import com.google.gson.annotations.SerializedName

data class GeocodingResponse(
    @SerializedName("name") val name: String,
    @SerializedName("lat")val lat: Double,
    @SerializedName("lon")val lon: Double
)
