package com.dreslan.countdown.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WidgetTickReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Trigger an immediate widget update after boot.
            // The Application class re-registers ACTION_TIME_TICK in onCreate(),
            // which runs when the system starts the app process after boot.
            CoroutineScope(Dispatchers.Default).launch {
                CountdownWidget().updateAll(context)
                CountdownWidgetSmall().updateAll(context)
            }
        }
    }
}
