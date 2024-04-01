package com.gracedian.explorebojonegoro.ui.dashboard.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.andrefrsousa.superbottomsheet.SuperBottomSheetFragment
import com.gracedian.explorebojonegoro.R
import com.warkiz.widget.IndicatorSeekBar

class FilterBottomSheetFragment :  SuperBottomSheetFragment() {

    private lateinit var spinnerCategory: Spinner
    private lateinit var ratingBar: RatingBar
    private lateinit var jarakMaxSeekBar: IndicatorSeekBar
    private lateinit var btnReset: Button
    private lateinit var btnApply: Button
    private lateinit var btnCancel: ImageView


    var onFilterApplied: ((String, Float, Int) -> Unit)? = null
    var onFilterReset: (() -> Unit)? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_filter_bottom_sheet, container, false)

        spinnerCategory = view.findViewById(R.id.spinnerCategory)
        ratingBar = view.findViewById(R.id.ratingBar)
        jarakMaxSeekBar = view.findViewById(R.id.jarakMax)
        btnReset = view.findViewById(R.id.btnReset)
        btnApply = view.findViewById(R.id.btnApply)
        btnCancel = view.findViewById(R.id.btnCancel)

        val categories = arrayOf("Pilih Kategori", "Wisata Alam", "Wisata Buatan", "Wisata Agro", "Wisata Edukasi", "Wisata Budaya dan Sejarah", "Wisata Religi")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter

        btnCancel.setOnClickListener {
            dismiss()
        }

        btnApply.setOnClickListener {
            val selectedCategory = if (spinnerCategory.selectedItem.toString() == "Pilih Kategori") {
                ""
            } else {
                spinnerCategory.selectedItem.toString()
            }
            val selectedRating = ratingBar.rating
            val selectedJarakMax = jarakMaxSeekBar.progress

            onFilterApplied?.invoke(selectedCategory, selectedRating, selectedJarakMax)

            SharedPrefManager.saveFilterPreferences(requireContext(), selectedCategory, selectedRating, selectedJarakMax)
            dismiss()
        }

        btnReset.setOnClickListener {
            onFilterReset?.invoke()
            resetFilter()
        }
        val (savedCategory, savedRating, savedJarakMax) = SharedPrefManager.getFilterPreferences(requireContext())

        if (!categories.contains(savedCategory)) {
            spinnerCategory.setSelection(0)
        } else {
            spinnerCategory.setSelection(adapter.getPosition(savedCategory))
        }
        ratingBar.rating = savedRating
        jarakMaxSeekBar.setProgress(savedJarakMax.toFloat())


        return view
    }

    private fun resetFilter() {
        SharedPrefManager.saveFilterPreferences(requireContext(), "Pilih Kategori", 0.0F, 0)
        spinnerCategory.setSelection(0)
        ratingBar.rating = 0.0F
        jarakMaxSeekBar.setProgress(0F)
    }



}
