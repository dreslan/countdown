package com.dreslan.countdown.widget

import android.content.Context

private const val PREFS_NAME = "countdown_widget_prefs"
private const val KEY_PREFIX = "widget_countdown_"

fun saveWidgetCountdownId(context: Context, appWidgetId: Int, countdownId: Long) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putLong("$KEY_PREFIX$appWidgetId", countdownId)
        .apply()
}

fun getWidgetCountdownId(context: Context, appWidgetId: Int): Long? {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val key = "$KEY_PREFIX$appWidgetId"
    return if (prefs.contains(key)) prefs.getLong(key, -1L) else null
}

fun removeWidgetCountdownId(context: Context, appWidgetId: Int) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .remove("$KEY_PREFIX$appWidgetId")
        .apply()
}
