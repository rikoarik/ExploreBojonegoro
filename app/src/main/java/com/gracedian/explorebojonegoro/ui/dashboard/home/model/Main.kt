package com.gracedian.explorebojonegoro.ui.dashboard.home.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Main(
    @SerializedName("temp")
    @Expose
    var temp: Double = 0.0,

    @SerializedName("humidity")
    @Expose
    var humidity: Int = 0
)