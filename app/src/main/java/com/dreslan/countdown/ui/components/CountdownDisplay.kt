package com.dreslan.countdown.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.dreslan.countdown.CountdownTime
import com.dreslan.countdown.calculateRemaining
import kotlinx.coroutines.delay
import java.time.Instant

@Composable
fun CountdownDisplay(
    targetDateTime: Instant,
    zeroMessage: String?,
    countdownStyle: TextStyle = MaterialTheme.typography.displayLarge,
    labelStyle: TextStyle = MaterialTheme.typography.labelSmall,
    modifier: Modifier = Modifier,
    onZeroCrossing: (() -> Unit)? = null
) {
    var time by remember { mutableStateOf(calculateRemaining(targetDateTime, Instant.now())) }
    var wasComplete by remember { mutableStateOf(time.isComplete) }

    LaunchedEffect(targetDateTime) {
        while (true) {
            time = calculateRemaining(targetDateTime, Instant.now())
            if (time.isComplete && !wasComplete) {
                wasComplete = true
                onZeroCrossing?.invoke()
            }
            delay(1000L)
        }
    }

    Column(modifier = modifier) {
        if (time.isComplete && !zeroMessage.isNullOrBlank()) {
            Text(
                text = zeroMessage,
                style = countdownStyle,
                color = MaterialTheme.colorScheme.onBackground
            )
        } else {
            Text(
                text = time.toDisplayString(),
                style = countdownStyle,
                color = MaterialTheme.colorScheme.onBackground
            )
            Row {
                UnitLabel("DAYS", labelStyle)
                Spacer(Modifier.width(12.dp))
                UnitLabel("HRS", labelStyle)
                Spacer(Modifier.width(12.dp))
                UnitLabel("MIN", labelStyle)
                Spacer(Modifier.width(12.dp))
                UnitLabel("SEC", labelStyle)
            }
        }
    }
}

@Composable
private fun UnitLabel(text: String, style: TextStyle) {
    Text(
        text = text,
        style = style,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
    )
}
