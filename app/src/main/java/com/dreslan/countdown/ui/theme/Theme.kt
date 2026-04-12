package com.dreslan.countdown.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import com.dreslan.countdown.data.CountdownTheme

private val CleanColorScheme = darkColorScheme(
    background = CleanColors.backgroundStart,
    surface = CleanColors.backgroundMid,
    onBackground = CleanColors.countdownText,
    onSurface = CleanColors.countdownText,
    primary = CleanColors.labelText,
    onPrimary = CleanColors.countdownText,
)

private val MedievalColorScheme = darkColorScheme(
    background = MedievalColors.backgroundStart,
    surface = MedievalColors.backgroundMid,
    onBackground = MedievalColors.countdownText,
    onSurface = MedievalColors.countdownText,
    primary = MedievalColors.labelText,
    onPrimary = MedievalColors.countdownText,
)

@Composable
fun CountdownAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CleanColorScheme,
        typography = CleanTypography,
        content = content
    )
}

@Composable
fun CountdownItemTheme(
    theme: CountdownTheme,
    content: @Composable () -> Unit
) {
    val colorScheme = when (theme) {
        CountdownTheme.CLEAN -> CleanColorScheme
        CountdownTheme.MEDIEVAL -> MedievalColorScheme
    }
    val typography = when (theme) {
        CountdownTheme.CLEAN -> CleanTypography
        CountdownTheme.MEDIEVAL -> MedievalTypography
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}
