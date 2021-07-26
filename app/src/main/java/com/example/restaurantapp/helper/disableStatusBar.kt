package com.example.restaurantapp.helper

import android.app.Activity
import android.content.Context
import android.graphics.PixelFormat
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.WindowManager


class customViewGroup(context: Context?) : ViewGroup(context) {
    override fun onLayout(
        changed: Boolean,
        l: Int,
        t: Int,
        r: Int,
        b: Int
    ) {
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        Log.v("customViewGroup", "**********Intercepted")
        return true
    }

    fun preventStatusBarExpansion(context: Context) {

    }
}

