package com.example.tusa_android.screen

import android.app.Activity
import android.graphics.Insets
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowInsets
import android.view.WindowMetrics


class ScreenMetricsUtils {
    companion object {

        private lateinit var displayMetrics: DisplayMetrics
        private lateinit var windowMetrics: WindowMetrics

        fun rememberMetrics(activity: Activity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                windowMetrics = activity.windowManager.currentWindowMetrics
            } else {
                displayMetrics = DisplayMetrics()
                activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
            }
        }

        fun getScreenHeigth(): Int {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                windowMetrics.bounds.height()
            } else {
                displayMetrics.heightPixels
            }
        }

        fun getScreenWidth(): Int {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val insets: Insets = windowMetrics.windowInsets
                    .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
                windowMetrics.bounds.width() - insets.left - insets.right
            } else {
                val displayMetrics = DisplayMetrics()
                displayMetrics.widthPixels
            }
        }
    }
}