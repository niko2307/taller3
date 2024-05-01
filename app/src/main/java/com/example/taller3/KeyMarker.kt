package com.example.taller3

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class KeyMarker(

    @SerializedName("latitude") @Expose var latitude: Double? = null,
    @SerializedName("longitude") @Expose var longitude: Double? = null,
    @SerializedName("name") @Expose var name: String? = null
)
