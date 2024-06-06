package com.gracedian.explorebojonegoro.ui.dashboard.home.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Main(
    @SerializedName("temp")
    @Expose
    var temp: Double,

    @SerializedName("humidity")
    @Expose
    var humidity: Int
)