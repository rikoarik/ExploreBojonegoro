package com.gracedian.explorebojonegoro.ui.navigateroute

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.widget.AppCompatButton
import com.andrefrsousa.superbottomsheet.SuperBottomSheetFragment
import com.gracedian.explorebojonegoro.R
import com.gracedian.explorebojonegoro.ui.dashboard.home.activity.DetailsWisataActivity
import com.gracedian.explorebojonegoro.ui.dashboard.home.fragmentdetail.activity.DetailsHotelActivity
import com.gracedian.explorebojonegoro.ui.dashboard.home.fragmentdetail.activity.DetailsRestoranActivity

class FinishNavigateDialog(private val destinationType: String) : SuperBottomSheetFragment() {

    private lateinit var btnBackToDetails: AppCompatButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.finish_navigating_dialog, container, false)
        btnBackToDetails = view.findViewById(R.id.btnBackToDetails)

        btnBackToDetails.setOnClickListener {
            val intent = when (destinationType) {
                "wisata" -> Intent(requireContext(), DetailsWisataActivity::class.java)
                "restoran" -> Intent(requireContext(), DetailsRestoranActivity::class.java)
                "hotel" -> Intent(requireContext(), DetailsHotelActivity::class.java)
                else -> null
            }
            if (intent != null) {
                startActivity(intent)
                requireActivity().finish()
            }

        }

        return view

    }
}