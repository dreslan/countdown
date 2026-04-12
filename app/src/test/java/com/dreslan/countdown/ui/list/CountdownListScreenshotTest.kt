package com.dreslan.countdown.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.dreslan.countdown.data.Countdown
import com.dreslan.countdown.data.CountdownTheme
import com.dreslan.countdown.ui.theme.CleanColors
import com.dreslan.countdown.ui.theme.CountdownAppTheme
import com.dreslan.countdown.ui.theme.CountdownItemTheme
import com.dreslan.countdown.ui.theme.MedievalColors
import org.junit.Rule
import org.junit.Test
import java.time.Instant

class CountdownListScreenshotTest {

    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_5)

    // Fixed future instant so screenshots are deterministic
    private val futureTarget = Instant.parse("2099-01-01T00:00:00Z")

    @Test
    fun emptyState() {
        paparazzi.snapshot {
            CountdownAppTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CleanColors.backgroundStart),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No countdowns yet.\nTap + to create one.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = CleanColors.labelText
                        )
                    }
                }
            }
        }
    }

    @Test
    fun cleanThemedCard() {
        val countdown = Countdown(
            id = 1L,
            title = "Mission Launch",
            targetDateTime = futureTarget,
            timeZone = "UTC",
            theme = CountdownTheme.CLEAN
        )
        paparazzi.snapshot {
            CountdownAppTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CleanColors.backgroundStart)
                        .padding(16.dp)
                ) {
                    StaticCountdownCard(countdown = countdown)
                }
            }
        }
    }

    @Test
    fun medievalThemedCard() {
        val countdown = Countdown(
            id = 2L,
            title = "Battle of Stirling",
            targetDateTime = futureTarget,
            timeZone = "UTC",
            theme = CountdownTheme.MEDIEVAL,
            zeroMessage = "FREEDOM!"
        )
        paparazzi.snapshot {
            CountdownAppTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CleanColors.backgroundStart)
                        .padding(16.dp)
                ) {
                    StaticCountdownCard(countdown = countdown)
                }
            }
        }
    }
}

/**
 * Static version of CountdownCard for screenshot tests — renders a fixed time string
 * instead of a live ticker, ensuring deterministic snapshots.
 */
@Composable
private fun StaticCountdownCard(countdown: Countdown) {
    val bgBrush = when (countdown.theme) {
        CountdownTheme.CLEAN -> Brush.linearGradient(
            listOf(CleanColors.backgroundMid, CleanColors.backgroundEnd)
        )
        CountdownTheme.MEDIEVAL -> Brush.linearGradient(
            listOf(MedievalColors.backgroundMid, MedievalColors.backgroundEnd)
        )
    }

    CountdownItemTheme(theme = countdown.theme) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(bgBrush)
                .padding(20.dp)
        ) {
            Text(
                text = countdown.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            // Static display — avoids LaunchedEffect / coroutine issues in Paparazzi
            Text(
                text = "27375:00:00:00",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = MaterialTheme.typography.displayLarge.fontSize * 0.6
                ),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp)
            )
            Row {
                Text(
                    text = "DAYS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "HRS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "MIN",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "SEC",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            }
        }
    }
}
