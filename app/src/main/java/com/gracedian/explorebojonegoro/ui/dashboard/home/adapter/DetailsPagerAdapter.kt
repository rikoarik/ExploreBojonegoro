package com.gracedian.explorebojonegoro.ui.dashboard.home.adapter

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.gracedian.explorebojonegoro.ui.dashboard.home.fragmentdetail.GaleriFragment
import com.gracedian.explorebojonegoro.ui.dashboard.home.fragmentdetail.PenginapanFragment
import com.gracedian.explorebojonegoro.ui.dashboard.home.fragmentdetail.RestoranFragment
import com.gracedian.explorebojonegoro.ui.dashboard.home.fragmentdetail.TentangFragment
import com.gracedian.explorebojonegoro.ui.dashboard.home.fragmentdetail.UlasanFragment

class DetailsPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    private var data: Bundle? = null
    private var fragmentList: List<Fragment> = emptyList()

    fun setFragmentList(list: List<Fragment>) {
        fragmentList = list
    }

    fun setData(data: Bundle) {
        this.data = data
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                val tentangFragment = TentangFragment()
                tentangFragment.arguments = data
                tentangFragment
            }
            1 -> {
                val galeriFragment = GaleriFragment()
                galeriFragment.arguments = data
                galeriFragment
            }
            2 -> {
                val penginapanFragment = PenginapanFragment()
                penginapanFragment.arguments = data
                penginapanFragment
            }
            3 -> {
                val restoranFragment = RestoranFragment()
                restoranFragment.arguments = data
                restoranFragment
            }
            4 -> {
                val ulasanFragment = UlasanFragment()
                ulasanFragment.arguments = data
                ulasanFragment
            }
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }

    override fun getItemCount(): Int {
        return 5
    }
}
