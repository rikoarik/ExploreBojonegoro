package com.gracedian.explorebojonegoro.ui.dashboard.home.fragmentdetail

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gracedian.explorebojonegoro.R

class UlasanFragment : Fragment() {

    private lateinit var rcUlasan: RecyclerView
    private lateinit var btAddUlasan: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ulasan, container, false)

        btAddUlasan = view.findViewById(R.id.btAddUlasan)
        rcUlasan = view.findViewById(R.id.rcUlasan)
        rcUlasan.layoutManager = LinearLayoutManager(requireContext())

        return view
    }
}
