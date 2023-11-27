package com.gracedian.explorebojonegoro.ui.onboarding.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gracedian.explorebojonegoro.R


class Onboardingpage2 : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_onboarding_page2, container, false)
    }

    companion object {
        fun newInstance(): Onboardingpage2 {
            return Onboardingpage2()
        }
    }

}