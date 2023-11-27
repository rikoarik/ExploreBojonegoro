package com.gracedian.explorebojonegoro.ui.onboarding.adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.gracedian.explorebojonegoro.R
import com.gracedian.explorebojonegoro.ui.onboarding.fragment.Onboardingpage1
import com.gracedian.explorebojonegoro.ui.onboarding.fragment.Onboardingpage2
import com.gracedian.explorebojonegoro.ui.onboarding.fragment.Onboardingpage3

class OnboardingPagerAdapter(
    fragmentActivity: FragmentActivity,
) : FragmentStateAdapter(fragmentActivity) {

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> Onboardingpage1.newInstance()
            1 -> Onboardingpage2.newInstance()
            2 -> Onboardingpage3.newInstance()
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }


    override fun getItemCount(): Int {
        return 3
    }
}
