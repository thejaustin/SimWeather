package com.thejaustin.simweather

import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator

object ViewAnimations {
    fun fadeIn(
        view: View,
        delay: Long = 0,
    ) {
        view.alpha = 0f
        view.visibility = View.VISIBLE
        view.animate()
            .alpha(1f)
            .setDuration(300)
            .setStartDelay(delay)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    fun slideUp(
        view: View,
        delay: Long = 0,
    ) {
        view.translationY = 100f
        view.alpha = 0f
        view.visibility = View.VISIBLE
        view.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(400)
            .setStartDelay(delay)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    fun slideInRight(
        view: View,
        delay: Long = 0,
    ) {
        view.translationX = 100f
        view.alpha = 0f
        view.visibility = View.VISIBLE
        view.animate()
            .translationX(0f)
            .alpha(1f)
            .setDuration(300)
            .setStartDelay(delay)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    fun animateWeatherCard(view: View) {
        view.scaleX = 0.9f
        view.scaleY = 0.9f
        view.alpha = 0f
        view.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(300)
            .setInterpolator(OvershootInterpolator())
            .start()
    }

    fun animateForecastItem(
        view: View,
        position: Int,
    ) {
        view.translationY = 50f
        view.alpha = 0f
        view.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(300)
            .setStartDelay(position * 30L)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    // SimCity specific pop-up animation
    fun popUp(
        view: View,
        delay: Long = 0,
    ) {
        view.scaleX = 0f
        view.scaleY = 0f
        view.visibility = View.VISIBLE
        view.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(400)
            .setStartDelay(delay)
            // Bouncy pop
            .setInterpolator(OvershootInterpolator(1.5f))
            .start()
    }
}
