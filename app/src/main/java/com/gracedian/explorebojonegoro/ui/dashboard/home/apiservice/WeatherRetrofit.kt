package com.gracedian.explorebojonegoro.ui.dashboard.home.apiservice

import com.gracedian.explorebojonegoro.ui.dashboard.home.response.WeatherResponse
import com.mapbox.maps.extension.style.expressions.dsl.generated.get
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherRetrofit {
    private val BASE_URL = "https://api.openweathermap.org/"
    private val apikey = "06e906f4c3a0cdc425878da5f07e5e23"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(OkHttpClient.Builder().build())
        .build()

    private val apiService = retrofit.create(ApiService::class.java)

    fun getWeatherData(latitude: Double, longitude: Double, callback: (WeatherResponse?) -> Unit) {
        val call = apiService.getWeather(latitude, longitude, apikey, "metric")
        call.enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.isSuccessful) {
                    val weatherResponse = response.body()
                    callback(weatherResponse)
                } else {
                    callback(null)
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                callback(null)
            }
        })
    }
}
