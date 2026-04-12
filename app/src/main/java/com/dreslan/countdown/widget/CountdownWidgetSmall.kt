package com.dreslan.countdown.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.provideContent
import com.dreslan.countdown.calculateRemaining
import com.dreslan.countdown.data.CountdownDatabase
import java.time.Instant

class CountdownWidgetSmall : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)
        val countdownId = getWidgetCountdownId(context, appWidgetId)
        val countdown = countdownId?.let {
            CountdownDatabase.getInstance(context).countdownDao().getById(it)
        }

        provideContent {
            if (countdown == null) {
                DeletedWidgetContent()
            } else {
                val time = calculateRemaining(countdown.targetDateTime, Instant.now())
                WidgetContent(
                    title = countdown.title,
                    displayText = if (time.isComplete && !countdown.zeroMessage.isNullOrBlank()) {
                        countdown.zeroMessage!!
                    } else {
                        time.toDisplayString()
                    },
                    showUnits = !(time.isComplete && !countdown.zeroMessage.isNullOrBlank()),
                    theme = countdown.theme,
                    hasVideo = countdown.videoUrl != null,
                    isLarge = false
                )
            }
        }
    }
}
