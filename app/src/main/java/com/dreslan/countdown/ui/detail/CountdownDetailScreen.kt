package com.dreslan.countdown.ui.detail

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dreslan.countdown.data.CountdownTheme
import com.dreslan.countdown.data.Note
import com.dreslan.countdown.ui.components.CountdownDisplay
import com.dreslan.countdown.ui.theme.CleanColors
import com.dreslan.countdown.ui.theme.CountdownItemTheme
import com.dreslan.countdown.ui.theme.MedievalColors
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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
    var showNoteDialog by remember { mutableStateOf(false) }
    var editingNote by remember { mutableStateOf<Note?>(null) }
    var showExportMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current

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

    val fabContainerColor = when (countdown.theme) {
        CountdownTheme.CLEAN -> CleanColors.labelText
        CountdownTheme.MEDIEVAL -> MedievalColors.labelText
    }

    val retroTvBodyColor = when (countdown.theme) {
        CountdownTheme.CLEAN -> CleanColors.backgroundEnd
        CountdownTheme.MEDIEVAL -> MedievalColors.backgroundEnd
    }
    val retroTvBorderColor = when (countdown.theme) {
        CountdownTheme.CLEAN -> CleanColors.unitText
        CountdownTheme.MEDIEVAL -> MedievalColors.unitText
    }
    val retroTvPlayIconColor = when (countdown.theme) {
        CountdownTheme.CLEAN -> CleanColors.playButton
        CountdownTheme.MEDIEVAL -> MedievalColors.playButton
    }

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
                        if (state.notes.isNotEmpty()) {
                            Box {
                                IconButton(onClick = { showExportMenu = true }) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "More options",
                                        tint = topBarAction
                                    )
                                }
                                DropdownMenu(
                                    expanded = showExportMenu,
                                    onDismissRequest = { showExportMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Export as Markdown") },
                                        onClick = {
                                            showExportMenu = false
                                            context.startActivity(viewModel.exportMarkdown())
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Export as JSON") },
                                        onClick = {
                                            showExportMenu = false
                                            context.startActivity(viewModel.exportJson())
                                        }
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = topBarBg
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        editingNote = null
                        showNoteDialog = true
                    },
                    containerColor = fabContainerColor
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add note")
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            val bgBitmap = countdown.backgroundImagePath?.let { path ->
                if (path.isNotBlank()) {
                    try { BitmapFactory.decodeFile(path)?.asImageBitmap() } catch (_: Exception) { null }
                } else null
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundBrush)
                    .padding(padding)
            ) {
                if (bgBitmap != null) {
                    Image(
                        bitmap = bgBitmap,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color(0xAA000000))
                    )
                }
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

                    val zone = ZoneId.of(countdown.timeZone)
                    val zdt = countdown.targetDateTime.atZone(zone)
                    val dateFormatter = DateTimeFormatter.ofPattern("MMMM d, h:mm a")
                    Text(
                        text = zdt.format(dateFormatter),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    )

                    CountdownDisplay(
                        targetDateTime = countdown.targetDateTime,
                        zeroMessage = countdown.zeroMessage,
                        countdownStyle = MaterialTheme.typography.displayLarge,
                        labelStyle = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (countdown.showProgress) {
                        val origin = countdown.startDate ?: countdown.createdAt
                        val startLabel = origin.atZone(zone).format(DateTimeFormatter.ofPattern("MMM d"))
                        val targetLabel = countdown.targetDateTime.atZone(zone).format(DateTimeFormatter.ofPattern("MMM d"))
                        TimelineBar(
                            startDate = origin,
                            targetDate = countdown.targetDateTime,
                            noteTimestamps = state.notes.map { it.createdAt },
                            progressColor = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surface,
                            dotColor = MaterialTheme.colorScheme.primary,
                            labelColor = MaterialTheme.colorScheme.onBackground,
                            startLabel = startLabel,
                            targetLabel = targetLabel,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (state.notes.isNotEmpty()) {
                        NoteFeed(
                            notes = state.notes,
                            timeZone = zone,
                            dotColor = MaterialTheme.colorScheme.primary,
                            lineColor = MaterialTheme.colorScheme.surface,
                            dateColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            textColor = MaterialTheme.colorScheme.onBackground,
                            onNoteClick = { note ->
                                editingNote = note
                                showNoteDialog = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 400.dp)
                        )
                    }

                    val videoUrl = countdown.videoUrl
                    if (!videoUrl.isNullOrBlank()) {
                        RetroTvButton(
                            embedUrl = videoUrl,
                            bodyColor = retroTvBodyColor,
                            borderColor = retroTvBorderColor,
                            playIconColor = retroTvPlayIconColor,
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

    if (showNoteDialog) {
        NoteDialog(
            existingNote = editingNote,
            timeZone = ZoneId.of(countdown.timeZone),
            onSave = { text, createdAt ->
                val existing = editingNote
                if (existing != null) {
                    viewModel.updateNote(existing, text, createdAt)
                } else {
                    viewModel.addNote(text, createdAt)
                }
                showNoteDialog = false
                editingNote = null
            },
            onDelete = editingNote?.let { note ->
                {
                    viewModel.deleteNote(note.id)
                    showNoteDialog = false
                    editingNote = null
                }
            },
            onDismiss = {
                showNoteDialog = false
                editingNote = null
            }
        )
    }
}
