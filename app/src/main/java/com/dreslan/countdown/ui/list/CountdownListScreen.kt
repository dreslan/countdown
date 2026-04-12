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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dreslan.countdown.data.Countdown
import com.dreslan.countdown.data.CountdownTheme
import com.dreslan.countdown.ui.components.CountdownDisplay
import com.dreslan.countdown.ui.theme.CleanColors
import com.dreslan.countdown.ui.theme.CountdownItemTheme
import com.dreslan.countdown.ui.theme.MedievalColors

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
                    CountdownCard(
                        countdown = countdown,
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
private fun CountdownCard(countdown: Countdown, onClick: () -> Unit) {
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
                .clickable(onClick = onClick)
                .padding(20.dp)
        ) {
            Text(
                text = countdown.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            CountdownDisplay(
                targetDateTime = countdown.targetDateTime,
                zeroMessage = countdown.zeroMessage,
                countdownStyle = MaterialTheme.typography.displayLarge.copy(
                    fontSize = MaterialTheme.typography.displayLarge.fontSize * 0.6
                ),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
