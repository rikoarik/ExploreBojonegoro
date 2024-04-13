package com.gracedian.explorebojonegoro.ui.navigateroute

import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Call

interface MapQuestService {
    @GET("directions/v2/route")
    fun getRoute(
        @Query("key") apiKey: String,
        @Query("from") origin: String,
        @Query("to") destination: String,
        @Query("outFormat") outFormat: String = "json",
        @Query("routeType") routeType: String = "fastest"
    ): Call<RouteResponse>
}
