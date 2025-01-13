package com.example.gediatest

import android.app.Application
import com.example.gediatest.sdk.PosSDK
import com.example.gediatest.ui.MainActivity

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        PosSDK.initPosSDK(this, MainActivity::class.java.name)
    }
}