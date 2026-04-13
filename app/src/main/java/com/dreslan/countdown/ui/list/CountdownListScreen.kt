package com.dreslan.countdown.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import android.graphics.BitmapFactory
import com.dreslan.countdown.data.Countdown
import com.dreslan.countdown.data.CountdownTheme
import com.dreslan.countdown.ui.components.CountdownDisplay
import com.dreslan.countdown.ui.theme.CleanColors
import com.dreslan.countdown.ui.theme.CountdownItemTheme
import com.dreslan.countdown.ui.theme.MedievalColors
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountdownListScreen(
    onCountdownClick: (Long) -> Unit,
    onCreateClick: () -> Unit,
    viewModel: CountdownListViewModel = viewModel()
) {
    val countdowns by viewModel.countdowns.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Braveheart Timer") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CleanColors.backgroundStart,
                    titleContentColor = CleanColors.countdownText
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateClick,
                containerColor = CleanColors.labelText
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create countdown")
            }
        },
        containerColor = CleanColors.backgroundStart
    ) { padding ->
        if (countdowns.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
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
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(countdowns, key = { it.id }) { countdown ->
                    val noteCount by viewModel.getNoteCount(countdown.id).collectAsState(initial = 0)
                    CountdownCard(
                        countdown = countdown,
                        noteCount = noteCount,
                        onClick = { onCountdownClick(countdown.id) }
                    )
                }
                // Version label at the bottom of the list
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "v0.1",
                            color = CleanColors.unitText,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CountdownCard(countdown: Countdown, noteCount: Int, onClick: () -> Unit) {
    val bgBrush = when (countdown.theme) {
        CountdownTheme.CLEAN -> Brush.linearGradient(
            listOf(CleanColors.backgroundMid, CleanColors.backgroundEnd)
        )
        CountdownTheme.MEDIEVAL -> Brush.linearGradient(
            listOf(MedievalColors.backgroundMid, MedievalColors.backgroundEnd)
        )
    }

    val bgBitmap = countdown.backgroundImagePath?.let { path ->
        if (path.isNotBlank()) {
            try { BitmapFactory.decodeFile(path)?.asImageBitmap() } catch (_: Exception) { null }
        } else null
    }

    CountdownItemTheme(theme = countdown.theme) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(bgBrush)
                .clickable(onClick = onClick)
        ) {
            // Background image if set
            if (bgBitmap != null) {
                Image(
                    bitmap = bgBitmap,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
                // Dark scrim for readability
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color(0xAA000000))
                )
            }

            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = countdown.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                if (!countdown.description.isNullOrBlank()) {
                    Text(
                        text = countdown.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
                CountdownDisplay(
                    targetDateTime = countdown.targetDateTime,
                    zeroMessage = countdown.zeroMessage,
                    countdownStyle = MaterialTheme.typography.displayLarge.copy(
                        fontSize = MaterialTheme.typography.displayLarge.fontSize * 0.6
                    ),
                    modifier = Modifier.padding(top = 8.dp)
                )

                if (countdown.showProgress) {
                    val origin = countdown.startDate ?: countdown.createdAt
                    val totalDuration = countdown.targetDateTime.toEpochMilli() - origin.toEpochMilli()
                    val elapsed = Instant.now().toEpochMilli() - origin.toEpochMilli()
                    val progress = (elapsed.toFloat() / totalDuration.toFloat()).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surface
                    )
                }
                if (countdown.showProgress && noteCount > 0) {
                    Text(
                        text = "$noteCount note${if (noteCount != 1) "s" else ""}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
