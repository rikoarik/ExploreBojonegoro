package com.gracedian.explorebojonegoro.ui.dashboard.home.fragmentdetail.items

data class Hotel(
    val nama: String? = null,
    val alamat: String? = null,
    val imageUrl: String? = null,
    val latitude: String? = null,
    val longitude: String? = null,
    var rating: Double?,
    val jarak: Int = 0
)

