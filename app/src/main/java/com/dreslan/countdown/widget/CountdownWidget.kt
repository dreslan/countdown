package com.dreslan.countdown.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontFamily
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.dreslan.countdown.MainActivity
import com.dreslan.countdown.calculateRemaining
import com.dreslan.countdown.data.CountdownDatabase
import com.dreslan.countdown.data.CountdownTheme
import com.dreslan.countdown.ui.theme.CleanColors
import com.dreslan.countdown.ui.theme.MedievalColors
import java.time.Instant

class CountdownWidget : GlanceAppWidget() {
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
                    isLarge = true
                )
            }
        }
    }
}

@Composable
fun WidgetContent(
    title: String,
    displayText: String,
    showUnits: Boolean,
    theme: CountdownTheme,
    hasVideo: Boolean,
    isLarge: Boolean
) {
    val bgColor = when (theme) {
        CountdownTheme.CLEAN -> CleanColors.backgroundMid
        CountdownTheme.MEDIEVAL -> MedievalColors.backgroundMid
    }
    val textColor = when (theme) {
        CountdownTheme.CLEAN -> CleanColors.countdownText
        CountdownTheme.MEDIEVAL -> MedievalColors.countdownText
    }
    val labelColor = when (theme) {
        CountdownTheme.CLEAN -> CleanColors.labelText
        CountdownTheme.MEDIEVAL -> MedievalColors.labelText
    }
    val unitColor = when (theme) {
        CountdownTheme.CLEAN -> CleanColors.unitText
        CountdownTheme.MEDIEVAL -> MedievalColors.unitText
    }
    val playColor = when (theme) {
        CountdownTheme.CLEAN -> CleanColors.playButton
        CountdownTheme.MEDIEVAL -> MedievalColors.playButton
    }
    val fontFamily = when (theme) {
        CountdownTheme.CLEAN -> FontFamily.SansSerif
        CountdownTheme.MEDIEVAL -> FontFamily.Serif
    }
    val fontWeight = when (theme) {
        CountdownTheme.CLEAN -> FontWeight.Normal
        CountdownTheme.MEDIEVAL -> FontWeight.Bold
    }

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(bgColor)
            .clickable(actionStartActivity<MainActivity>())
            .padding(horizontal = 20.dp, vertical = if (isLarge) 16.dp else 8.dp)
    ) {
        Row(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = title,
                    style = TextStyle(
                        color = ColorProvider(labelColor),
                        fontSize = 10.sp,
                        fontFamily = fontFamily
                    )
                )
                Spacer(GlanceModifier.height(4.dp))
                Text(
                    text = displayText,
                    style = TextStyle(
                        color = ColorProvider(textColor),
                        fontSize = if (isLarge) 28.sp else 22.sp,
                        fontWeight = fontWeight,
                        fontFamily = fontFamily
                    )
                )
                if (showUnits) {
                    Spacer(GlanceModifier.height(2.dp))
                    Text(
                        text = "DAYS    HRS     MIN     SEC",
                        style = TextStyle(
                            color = ColorProvider(unitColor),
                            fontSize = 8.sp,
                            fontFamily = fontFamily
                        )
                    )
                }
            }

            if (isLarge && hasVideo) {
                Spacer(GlanceModifier.width(12.dp))
                Box(
                    modifier = GlanceModifier
                        .size(48.dp)
                        .clickable(actionStartActivity<MainActivity>()),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "\u25B6",
                        style = TextStyle(
                            color = ColorProvider(playColor),
                            fontSize = 24.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun DeletedWidgetContent() {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(CleanColors.backgroundMid)
            .clickable(actionStartActivity<MainActivity>())
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Countdown deleted\nTap to reconfigure",
            style = TextStyle(
                color = ColorProvider(CleanColors.labelText),
                fontSize = 12.sp
            )
        )
    }
}
