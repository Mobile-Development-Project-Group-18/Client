package com.group18.gosell

import android.app.Application
import com.group18.gosell.data.utils.CloudinaryManager

class GoSellApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        CloudinaryManager.init(applicationContext)
    }
}