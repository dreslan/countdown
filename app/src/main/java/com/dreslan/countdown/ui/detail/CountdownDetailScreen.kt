package com.dreslan.countdown.ui.detail

import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dreslan.countdown.data.CountdownTheme
import com.dreslan.countdown.ui.components.CountdownDisplay
import com.dreslan.countdown.ui.theme.CleanColors
import com.dreslan.countdown.ui.theme.CountdownItemTheme
import com.dreslan.countdown.ui.theme.MedievalColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountdownDetailScreen(
    countdownId: Long,
    autoPlayVideo: Boolean = false,
    onNavigateBack: () -> Unit,
    onEditClick: (Long) -> Unit,
    viewModel: CountdownDetailViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var shouldAutoPlay by remember { mutableStateOf(autoPlayVideo) }

    LaunchedEffect(countdownId) {
        viewModel.loadCountdown(countdownId)
    }

    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) {
            onNavigateBack()
        }
    }

    val countdown = state.countdown ?: return

    val backgroundBrush = when (countdown.theme) {
        CountdownTheme.CLEAN -> Brush.verticalGradient(
            listOf(
                CleanColors.backgroundStart,
                CleanColors.backgroundMid,
                CleanColors.backgroundEnd
            )
        )
        CountdownTheme.MEDIEVAL -> Brush.verticalGradient(
            listOf(
                MedievalColors.backgroundStart,
                MedievalColors.backgroundMid,
                MedievalColors.backgroundEnd
            )
        )
    }

    val topBarColors = when (countdown.theme) {
        CountdownTheme.CLEAN -> Triple(
            CleanColors.backgroundStart,
            CleanColors.countdownText,
            CleanColors.labelText
        )
        CountdownTheme.MEDIEVAL -> Triple(
            MedievalColors.backgroundStart,
            MedievalColors.countdownText,
            MedievalColors.labelText
        )
    }
    val (topBarBg, topBarContent, topBarAction) = topBarColors

    CountdownItemTheme(theme = countdown.theme) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = topBarContent
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { onEditClick(countdown.id) }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = topBarAction
                            )
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = topBarAction
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = topBarBg
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundBrush)
                    .padding(padding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = countdown.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.fillMaxWidth()
                    )

                    CountdownDisplay(
                        targetDateTime = countdown.targetDateTime,
                        zeroMessage = countdown.zeroMessage,
                        countdownStyle = MaterialTheme.typography.displayLarge,
                        labelStyle = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.fillMaxWidth(),
                        onZeroCrossing = { shouldAutoPlay = true }
                    )

                    val videoUrl = countdown.videoUrl
                    if (!videoUrl.isNullOrBlank()) {
                        YoutubePlayer(
                            embedUrl = videoUrl,
                            autoPlay = shouldAutoPlay,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete countdown?") },
            text = { Text("\"${countdown.title}\" will be permanently deleted.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteCountdown()
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun YoutubePlayer(
    embedUrl: String,
    autoPlay: Boolean,
    modifier: Modifier = Modifier
) {
    val autoplayParam = if (autoPlay) "?autoplay=1" else ""
    val html = """
        <html>
        <body style="margin:0;padding:0;background:#000;">
        <iframe
            width="100%%" height="100%%"
            src="${embedUrl}${autoplayParam}"
            frameborder="0"
            allow="autoplay; encrypted-media"
            allowfullscreen>
        </iframe>
        </body>
        </html>
    """.trimIndent()

    Box(
        modifier = modifier
            .aspectRatio(16f / 9f)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
    ) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    webViewClient = WebViewClient()
                    webChromeClient = WebChromeClient()
                    settings.javaScriptEnabled = true
                    settings.mediaPlaybackRequiresUserGesture = !autoPlay
                    settings.domStorageEnabled = true
                    settings.cacheMode = WebSettings.LOAD_DEFAULT
                    loadDataWithBaseURL(
                        "https://www.youtube.com",
                        html,
                        "text/html",
                        "UTF-8",
                        null
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
