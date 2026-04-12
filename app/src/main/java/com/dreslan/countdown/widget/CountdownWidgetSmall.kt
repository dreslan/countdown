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
import com.dreslan.countdown.data.CountdownDatabase
import com.dreslan.countdown.data.CountdownTheme
import com.dreslan.countdown.ui.theme.CleanColors
import com.dreslan.countdown.ui.theme.MedievalColors
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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
                val zone = ZoneId.of(countdown.timeZone)
                val zdt = countdown.targetDateTime.atZone(zone)
                val dateFormatter = DateTimeFormatter.ofPattern("MMM d, h:mm a")
                val formattedDate = zdt.format(dateFormatter)

                WidgetContent(
                    title = countdown.title,
                    targetDate = formattedDate,
                    theme = countdown.theme,
                    hasVideo = countdown.videoUrl != null
                )
            }
        }
    }
}

@Composable
private fun WidgetContent(
    title: String,
    targetDate: String,
    theme: CountdownTheme,
    hasVideo: Boolean
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
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = "Time remaining until $title",
                    style = TextStyle(
                        color = ColorProvider(labelColor),
                        fontSize = 11.sp,
                        fontFamily = fontFamily
                    ),
                    maxLines = 1
                )
                Spacer(GlanceModifier.height(4.dp))
                Text(
                    text = targetDate,
                    style = TextStyle(
                        color = ColorProvider(textColor),
                        fontSize = 18.sp,
                        fontWeight = fontWeight,
                        fontFamily = fontFamily
                    )
                )
            }

            if (hasVideo) {
                Spacer(GlanceModifier.width(8.dp))
                Box(
                    modifier = GlanceModifier.size(36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "\u25B6",
                        style = TextStyle(
                            color = ColorProvider(playColor),
                            fontSize = 20.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun DeletedWidgetContent() {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(CleanColors.backgroundMid)
            .clickable(actionStartActivity<MainActivity>())
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Countdown deleted — tap to reconfigure",
            style = TextStyle(
                color = ColorProvider(CleanColors.labelText),
                fontSize = 11.sp
            )
        )
    }
}
