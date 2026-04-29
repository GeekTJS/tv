package com.familytv.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FamilyTVApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
