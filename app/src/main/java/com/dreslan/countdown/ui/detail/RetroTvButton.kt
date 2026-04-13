package com.dreslan.countdown.ui.detail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlin.math.sin

@Composable
fun RetroTvButton(
    embedUrl: String,
    bodyColor: Color,
    borderColor: Color,
    screenColor: Color = Color(0xFF111111),
    playIconColor: Color,
    modifier: Modifier = Modifier
) {
    val videoId = embedUrl.substringAfterLast("/").substringBefore("?")
    val context = LocalContext.current

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .width(260.dp)
                .clickable {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://www.youtube.com/watch?v=$videoId")
                    )
                    context.startActivity(intent)
                }
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Antennas
                Canvas(
                    modifier = Modifier
                        .width(260.dp)
                        .height(35.dp)
                ) {
                    val centerX = size.width / 2
                    val antennaSpread = 65.dp.toPx()

                    drawLine(
                        color = borderColor,
                        start = Offset(centerX - 15.dp.toPx(), size.height),
                        end = Offset(centerX - antennaSpread, 6.dp.toPx()),
                        strokeWidth = 2.dp.toPx()
                    )
                    drawCircle(
                        color = borderColor,
                        radius = 4.dp.toPx(),
                        center = Offset(centerX - antennaSpread, 4.dp.toPx())
                    )

                    drawLine(
                        color = borderColor,
                        start = Offset(centerX + 15.dp.toPx(), size.height),
                        end = Offset(centerX + antennaSpread, 6.dp.toPx()),
                        strokeWidth = 2.dp.toPx()
                    )
                    drawCircle(
                        color = borderColor,
                        radius = 4.dp.toPx(),
                        center = Offset(centerX + antennaSpread, 4.dp.toPx())
                    )
                }

                // TV body
                Box(
                    modifier = Modifier
                        .width(230.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(bodyColor)
                        .padding(10.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Screen
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(screenColor)
                                .drawBehind {
                                    var y = 0f
                                    while (y < size.height) {
                                        drawLine(
                                            color = Color.White.copy(alpha = 0.04f),
                                            start = Offset(0f, y),
                                            end = Offset(size.width, y),
                                            strokeWidth = 1f
                                        )
                                        y += 3.dp.toPx()
                                    }
                                    val step = 4.dp.toPx()
                                    var nx = 0f
                                    while (nx < size.width) {
                                        var ny = 0f
                                        while (ny < size.height) {
                                            val noise = sin(nx * 127.1 + ny * 311.7).let {
                                                (it * 43758.5453).let { v -> v - v.toLong() }
                                            }
                                            if (noise > 0.7f) {
                                                drawCircle(
                                                    color = Color.White.copy(alpha = 0.06f),
                                                    radius = 0.5.dp.toPx(),
                                                    center = Offset(nx, ny)
                                                )
                                            }
                                            ny += step
                                        }
                                        nx += step
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color.White.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play video",
                                    tint = playIconColor,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        // Knobs
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(10.dp).background(borderColor, CircleShape))
                            Box(modifier = Modifier.width(16.dp))
                            Box(modifier = Modifier.size(10.dp).background(borderColor, CircleShape))
                        }
                    }
                }

                // Pedestal feet
                Row(
                    modifier = Modifier.width(230.dp),
                    horizontalArrangement = Arrangement.spacedBy(110.dp, Alignment.CenterHorizontally)
                ) {
                    Box(
                        modifier = Modifier
                            .width(28.dp)
                            .height(10.dp)
                            .clip(RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
                            .background(bodyColor)
                    )
                    Box(
                        modifier = Modifier
                            .width(28.dp)
                            .height(10.dp)
                            .clip(RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
                            .background(bodyColor)
                    )
                }
            }
        }
    }
}
