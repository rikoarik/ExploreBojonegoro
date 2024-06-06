package com.gracedian.explorebojonegoro.ui.onboarding

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.isVisible
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.gracedian.explorebojonegoro.R
import com.gracedian.explorebojonegoro.ui.onboarding.adapter.OnboardingPagerAdapter
import com.gracedian.explorebojonegoro.utils.permissionrequest.PermissionRequestsActivity


class OnboardingActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var btNext: AppCompatButton
    private lateinit var btSkip: TextView
    private lateinit var linearLayout: LinearLayout
    private lateinit var llFinish: LinearLayout
    private lateinit var btFinish: AppCompatButton
    private lateinit var imgIlustrasi: ImageView
    private var doubleBackToExitPressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_onboarding)

        imgIlustrasi = findViewById(R.id.ilustrasi)
        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.pageIndicator)
        btNext = findViewById(R.id.btNext)
        btSkip = findViewById(R.id.btSkip)
        llFinish = findViewById(R.id.llfinish)
        linearLayout = findViewById(R.id.ll)
        btFinish = findViewById(R.id.btFinish)

        setupViews()
    }


    private fun setupViews() {
        val adapter = OnboardingPagerAdapter(this)
        viewPager.adapter = adapter
        TabLayoutMediator(tabLayout, viewPager) { _, _ -> }.attach()

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateIllustration(position)
                llFinish.isVisible = position == 2
                linearLayout.isVisible = position < 2
            }
        })

        btSkip.setOnClickListener {
            viewPager.setCurrentItem(2, true)
        }

        btNext.setOnClickListener {
            val currentItem = viewPager.currentItem
            updateIllustration(currentItem + 1)
            viewPager.setCurrentItem(currentItem + 1, true)
        }

        btFinish.setOnClickListener {
            val intent = Intent(this, PermissionRequestsActivity::class.java)
            startActivity(intent)
            finish()
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (doubleBackToExitPressedOnce) {
                    finish()
                    return
                }

                if (viewPager.currentItem > 0) {
                    viewPager.setCurrentItem(viewPager.currentItem - 1, true)
                } else {
                    doubleBackToExitPressedOnce = true
                    Toast.makeText(this@OnboardingActivity, "Please click BACK again to exit", Toast.LENGTH_SHORT).show()
                    Handler(Looper.getMainLooper()).postDelayed({
                        doubleBackToExitPressedOnce = false
                    }, 2000)
                }
            }
        })

    }

    fun updateIllustration(position: Int) {
        val newImageResId = when (position) {
            0 -> R.drawable.ilustrasi1
            1 -> R.drawable.ilustrasi2
            else -> R.drawable.ilustrasi3
            }
        crossfadeImages(newImageResId)
    }

    private fun crossfadeImages(newImageResId: Int) {
        val fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val fadeOutAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_out)

        imgIlustrasi.startAnimation(fadeOutAnimation)
        fadeOutAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) {
                imgIlustrasi.setImageResource(newImageResId)
                imgIlustrasi.startAnimation(fadeInAnimation)
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })
    }



}