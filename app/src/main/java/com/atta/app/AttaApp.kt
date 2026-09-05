package com.atta.app

import android.app.Application
import com.atta.app.notify.NotificationHelper

class AttaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.ensureChannel(this)
    }
}
