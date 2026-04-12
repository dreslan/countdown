package com.dreslan.countdown

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import androidx.glance.appwidget.updateAll
import com.dreslan.countdown.widget.CountdownWidget
import com.dreslan.countdown.widget.CountdownWidgetSmall

class CountdownApp : Application() {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val tickReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_TIME_TICK) {
                appScope.launch {
                    CountdownWidget().updateAll(context)
                    CountdownWidgetSmall().updateAll(context)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        registerReceiver(tickReceiver, IntentFilter(Intent.ACTION_TIME_TICK))
    }
}
