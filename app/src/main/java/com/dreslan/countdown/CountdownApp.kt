package com.dreslan.countdown

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.dreslan.countdown.widget.WidgetUpdateWorker
import java.util.concurrent.TimeUnit

class CountdownApp : Application() {
    override fun onCreate() {
        super.onCreate()

        val widgetUpdateRequest = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
            15, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "widget_update",
            ExistingPeriodicWorkPolicy.KEEP,
            widgetUpdateRequest
        )
    }
}
