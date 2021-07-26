package com.example.restaurantapp

import android.content.ComponentName
import android.content.Context


import android.app.admin.DeviceAdminReceiver
import android.content.Intent
import android.util.Log

class DeviceAdminReceiver: DeviceAdminReceiver() {
    override fun onEnabled(context: Context?, intent: Intent?) {
        super.onEnabled(context, intent)
        Log.d("hd", "Device Owner Enabled")
    }

    companion object {
        fun getComponentName(context: Context): ComponentName{
            return ComponentName(context.applicationContext, DeviceAdminReceiver::class.java)
        }
    }
}