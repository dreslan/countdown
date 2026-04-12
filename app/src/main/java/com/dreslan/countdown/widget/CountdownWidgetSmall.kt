package com.dreslan.countdown.widget

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
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
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class CountdownWidgetSmall : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)
        val countdownId = getWidgetCountdownId(context, appWidgetId)
        val countdown = countdownId?.let {
            CountdownDatabase.getInstance(context).countdownDao().getById(it)
        }

        val bgBitmap = countdown?.backgroundImagePath?.let { path ->
            if (path.isNotBlank()) {
                try { BitmapFactory.decodeFile(path) } catch (_: Exception) { null }
            } else null
        }

        provideContent {
            if (countdown == null) {
                DeletedWidgetContent()
            } else {
                val zone = ZoneId.of(countdown.timeZone)
                val zdt = countdown.targetDateTime.atZone(zone)
                val dateFormatter = DateTimeFormatter.ofPattern("MMM d, h:mm a")
                val formattedDate = zdt.format(dateFormatter)

                val now = Instant.now()
                val duration = Duration.between(now, countdown.targetDateTime)
                val coarseText = if (duration.isNegative || duration.isZero) {
                    countdown.zeroMessage ?: "Complete!"
                } else {
                    val days = duration.toDays()
                    val hours = duration.toHours() % 24
                    val minutes = duration.toMinutes() % 60
                    "${days}d ${hours}h ${minutes}m"
                }

                val progress = if (countdown.showProgress) {
                    val origin = countdown.startDate ?: countdown.createdAt
                    val total = countdown.targetDateTime.toEpochMilli() - origin.toEpochMilli()
                    val elapsed = now.toEpochMilli() - origin.toEpochMilli()
                    if (total > 0) (elapsed.toFloat() / total.toFloat()).coerceIn(0f, 1f) else 1f
                } else null

                WidgetContent(
                    title = countdown.title,
                    coarseCountdown = coarseText,
                    targetDate = formattedDate,
                    theme = countdown.theme,
                    videoUrl = countdown.videoUrl,
                    bgBitmap = bgBitmap?.let { ImageProvider(it) },
                    progress = progress
                )
            }
        }
    }
}

@Composable
private fun WidgetContent(
    title: String,
    coarseCountdown: String,
    targetDate: String,
    theme: CountdownTheme,
    videoUrl: String?,
    bgBitmap: ImageProvider?,
    progress: Float?
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
    val progressColor = when (theme) {
        CountdownTheme.CLEAN -> Color(0xFF8B949E)
        CountdownTheme.MEDIEVAL -> Color(0xFFD4A855)
    }

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .clickable(actionStartActivity<MainActivity>())
    ) {
        // Layer 1: Background — image or solid color
        if (bgBitmap != null) {
            Image(
                provider = bgBitmap,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = GlanceModifier.fillMaxSize()
            )
        } else {
            Box(modifier = GlanceModifier.fillMaxSize().background(bgColor)) {}
        }

        // Layer 2: Dark scrim overlay (only when image is present)
        if (bgBitmap != null) {
            Box(modifier = GlanceModifier.fillMaxSize().background(Color(0xAA000000))) {}
        }

        // Layer 3: Content
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: text content
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        text = "Countdown to $title",
                        style = TextStyle(
                            color = ColorProvider(labelColor),
                            fontSize = 11.sp,
                            fontFamily = fontFamily
                        ),
                        maxLines = 1
                    )
                    Spacer(GlanceModifier.height(4.dp))
                    Text(
                        text = "$coarseCountdown \u00B7 $targetDate",
                        style = TextStyle(
                            color = ColorProvider(textColor),
                            fontSize = 16.sp,
                            fontWeight = fontWeight,
                            fontFamily = fontFamily
                        ),
                        maxLines = 1
                    )
                }

                // Right: action buttons
                Spacer(GlanceModifier.width(8.dp))
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Play button — opens YouTube directly
                    if (videoUrl != null) {
                        Box(
                            modifier = GlanceModifier
                                .size(56.dp)
                                .clickable(
                                    actionRunCallback<PlayVideoAction>(
                                        androidx.glance.action.actionParametersOf(
                                            PlayVideoAction.VIDEO_URL_KEY to videoUrl
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "\u25B6",
                                style = TextStyle(
                                    color = ColorProvider(playColor),
                                    fontSize = 32.sp
                                )
                            )
                        }
                    }
                    // Refresh button
                    Box(
                        modifier = GlanceModifier
                            .size(40.dp)
                            .clickable(actionRunCallback<RefreshAction>()),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "\u21BB",
                            style = TextStyle(
                                color = ColorProvider(labelColor),
                                fontSize = 22.sp
                            )
                        )
                    }
                }
            }

            // Progress bar at bottom
            if (progress != null) {
                Spacer(GlanceModifier.height(4.dp))
                val progressWidthDp = (280 * progress).dp
                Box(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(Color(0x33FFFFFF))
                ) {
                    Spacer(
                        modifier = GlanceModifier
                            .width(progressWidthDp)
                            .height(3.dp)
                            .background(progressColor)
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
            text = "Countdown deleted \u2014 tap to reconfigure",
            style = TextStyle(
                color = ColorProvider(CleanColors.labelText),
                fontSize = 11.sp
            )
        )
    }
}
