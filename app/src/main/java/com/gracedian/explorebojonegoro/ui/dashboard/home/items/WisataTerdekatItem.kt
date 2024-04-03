package com.gracedian.explorebojonegoro.ui.dashboard.home.items

import com.google.gson.annotations.SerializedName

data class WisataTerdekatItem(
    val imageUrl: String?,
    val wisata: String?,
    var rating: Double?,
    val alamat: String?,
    var jarak: Int?,
    val lat: Double?,
    val long: Double?,
    var isFavorite: Boolean = false
)

