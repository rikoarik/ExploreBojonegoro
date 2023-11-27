package com.gracedian.explorebojonegoro.ui.dashboard.home.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Weather (
    @SerializedName("description")
    @Expose
    var description: String? = null,

    @SerializedName("icon")
    @Expose
    var icon: String? = null
)