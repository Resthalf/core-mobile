package com.resthalflab.resthalfapp

import android.app.Application
import com.resthalflab.resthalfapp.app.initKoin

class ResthalfApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin(applicationContext)
    }
}
