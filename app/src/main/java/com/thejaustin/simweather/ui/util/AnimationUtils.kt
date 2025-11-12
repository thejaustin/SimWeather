package com.thejaustin.simweather.ui.util

import android.view.View
import android.view.animation.AnimationUtils
import com.thejaustin.simweather.R

object ViewAnimations {

    fun fadeIn(view: View, delay: Long = 0) {
        val animation = AnimationUtils.loadAnimation(view.context, R.anim.fade_in)
        animation.startOffset = delay
        view.startAnimation(animation)
    }

    fun slideUp(view: View, delay: Long = 0) {
        val animation = AnimationUtils.loadAnimation(view.context, R.anim.slide_up)
        animation.startOffset = delay
        view.startAnimation(animation)
    }

    fun slideInRight(view: View, delay: Long = 0) {
        val animation = AnimationUtils.loadAnimation(view.context, R.anim.slide_in_right)
        animation.startOffset = delay
        view.startAnimation(animation)
    }

    fun animateWeatherCard(view: View) {
        slideUp(view, 100)
    }

    fun animateForecastItem(view: View, position: Int) {
        slideInRight(view, position * 50L)
    }
}
