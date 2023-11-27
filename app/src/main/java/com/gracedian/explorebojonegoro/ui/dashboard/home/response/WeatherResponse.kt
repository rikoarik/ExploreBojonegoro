package com.gracedian.explorebojonegoro.ui.dashboard.home.response

import com.google.gson.annotations.SerializedName
import com.gracedian.explorebojonegoro.ui.dashboard.home.model.Main
import com.gracedian.explorebojonegoro.ui.dashboard.home.model.Weather

data class WeatherResponse(
    @SerializedName("main") val main: Main,
    @SerializedName("weather") val weather: List<Weather>,
)