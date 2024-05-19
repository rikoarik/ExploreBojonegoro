package com.gracedian.explorebojonegoro.ui.dashboard.home.items

data class SearchItem (
    val imageUrl: String?,
    val wisata: String?,
    val kategori: String?,
    var rating: Double? = 0.0,
    val alamat: String?,
    var jarak: Int?,
    var isFavorite: Boolean = false
)