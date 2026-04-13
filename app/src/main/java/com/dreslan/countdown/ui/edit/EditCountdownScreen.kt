package com.dreslan.countdown.ui.edit

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dreslan.countdown.data.CountdownTheme
import com.dreslan.countdown.data.deleteImage
import com.dreslan.countdown.data.saveImageToInternal
import com.dreslan.countdown.ui.theme.CleanColors
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCountdownScreen(
    countdownId: Long?,
    onNavigateBack: () -> Unit,
    viewModel: EditCountdownViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(countdownId) {
        if (countdownId != null && countdownId > 0L) {
            viewModel.loadCountdown(countdownId)
        }
    }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            onNavigateBack()
        }
    }

    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val path = saveImageToInternal(context, uri)
            if (path != null) {
                viewModel.updateBackgroundImage(path)
            }
        }
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }

    val dateFormatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM) }
    val timeFormatter = remember { DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (state.isEditing) "Edit Countdown" else "New Countdown",
                        color = CleanColors.countdownText
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = CleanColors.countdownText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CleanColors.backgroundStart
                )
            )
        },
        containerColor = CleanColors.backgroundStart
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title
            OutlinedTextField(
                value = state.title,
                onValueChange = viewModel::updateTitle,
                label = { Text("Title *", color = CleanColors.labelText) },
                isError = state.titleError != null,
                supportingText = state.titleError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = CleanColors.countdownText,
                    unfocusedTextColor = CleanColors.countdownText,
                    focusedBorderColor = CleanColors.labelText,
                    unfocusedBorderColor = CleanColors.unitText,
                    cursorColor = CleanColors.countdownText,
                )
            )

            // Description
            OutlinedTextField(
                value = state.description,
                onValueChange = viewModel::updateDescription,
                label = { Text("Description (optional)", color = CleanColors.labelText) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = CleanColors.countdownText,
                    unfocusedTextColor = CleanColors.countdownText,
                    focusedBorderColor = CleanColors.labelText,
                    unfocusedBorderColor = CleanColors.unitText,
                    cursorColor = CleanColors.countdownText,
                )
            )

            // Date + Time row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = CleanColors.countdownText
                    )
                ) {
                    Text(state.date.format(dateFormatter))
                }

                OutlinedButton(
                    onClick = { showTimePicker = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = CleanColors.countdownText
                    )
                ) {
                    Text(state.time.format(timeFormatter))
                }
            }

            // Time zone
            OutlinedTextField(
                value = state.timeZone.id,
                onValueChange = { id ->
                    try {
                        viewModel.updateTimeZone(ZoneId.of(id))
                    } catch (_: Exception) {
                        // ignore invalid input while typing
                    }
                },
                label = { Text("Time Zone", color = CleanColors.labelText) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = CleanColors.countdownText,
                    unfocusedTextColor = CleanColors.countdownText,
                    focusedBorderColor = CleanColors.labelText,
                    unfocusedBorderColor = CleanColors.unitText,
                    cursorColor = CleanColors.countdownText,
                )
            )

            // Theme toggle
            Column {
                Text(
                    text = "Theme",
                    style = MaterialTheme.typography.labelMedium,
                    color = CleanColors.labelText
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = state.theme == CountdownTheme.CLEAN,
                        onClick = { viewModel.updateTheme(CountdownTheme.CLEAN) },
                        label = { Text("Clean") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CleanColors.labelText,
                            selectedLabelColor = CleanColors.backgroundStart,
                            labelColor = CleanColors.countdownText,
                            containerColor = CleanColors.backgroundMid,
                        )
                    )
                    FilterChip(
                        selected = state.theme == CountdownTheme.MEDIEVAL,
                        onClick = { viewModel.updateTheme(CountdownTheme.MEDIEVAL) },
                        label = { Text("Medieval") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CleanColors.labelText,
                            selectedLabelColor = CleanColors.backgroundStart,
                            labelColor = CleanColors.countdownText,
                            containerColor = CleanColors.backgroundMid,
                        )
                    )
                }
            }

            // Zero message
            OutlinedTextField(
                value = state.zeroMessage,
                onValueChange = viewModel::updateZeroMessage,
                label = { Text("Message at zero (optional)", color = CleanColors.labelText) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = CleanColors.countdownText,
                    unfocusedTextColor = CleanColors.countdownText,
                    focusedBorderColor = CleanColors.labelText,
                    unfocusedBorderColor = CleanColors.unitText,
                    cursorColor = CleanColors.countdownText,
                )
            )

            // Video URL
            OutlinedTextField(
                value = state.videoUrl,
                onValueChange = viewModel::updateVideoUrl,
                label = { Text("YouTube URL (optional)", color = CleanColors.labelText) },
                isError = state.videoUrlError != null,
                supportingText = state.videoUrlError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = CleanColors.countdownText,
                    unfocusedTextColor = CleanColors.countdownText,
                    focusedBorderColor = CleanColors.labelText,
                    unfocusedBorderColor = CleanColors.unitText,
                    cursorColor = CleanColors.countdownText,
                )
            )

            // Show progress bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Show progress bar",
                    style = MaterialTheme.typography.bodyLarge,
                    color = CleanColors.countdownText
                )
                Switch(
                    checked = state.showProgress,
                    onCheckedChange = viewModel::updateShowProgress,
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = CleanColors.labelText,
                        checkedThumbColor = CleanColors.countdownText,
                        uncheckedTrackColor = CleanColors.backgroundMid,
                        uncheckedThumbColor = CleanColors.unitText
                    )
                )
            }

            // Start date + time (visible when progress bar is enabled)
            if (state.showProgress) {
                Column {
                    Text(
                        text = "Start Date",
                        style = MaterialTheme.typography.labelMedium,
                        color = CleanColors.labelText
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showStartDatePicker = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = CleanColors.countdownText
                            )
                        ) {
                            Text(state.startDate.format(dateFormatter))
                        }
                        OutlinedButton(
                            onClick = { showStartTimePicker = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = CleanColors.countdownText
                            )
                        ) {
                            Text(state.startTime.format(timeFormatter))
                        }
                    }
                }
            }

            // Background image
            Column {
                Text(
                    text = "Widget Background Image",
                    style = MaterialTheme.typography.labelMedium,
                    color = CleanColors.labelText
                )
                Spacer(modifier = Modifier.height(8.dp))
                val currentImagePath = state.backgroundImagePath
                if (currentImagePath != null) {
                    val fileName = File(currentImagePath).name
                    Text(
                        text = fileName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = CleanColors.countdownText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = CleanColors.countdownText
                            )
                        ) {
                            Text("Change")
                        }
                        OutlinedButton(
                            onClick = {
                                state.backgroundImagePath?.let { deleteImage(it) }
                                viewModel.updateBackgroundImage(null)
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = CleanColors.countdownText
                            )
                        ) {
                            Text("Remove")
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = CleanColors.countdownText
                        )
                    ) {
                        Text("Choose Background Image")
                    }
                }
            }

            // Save button
            Button(
                onClick = viewModel::save,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CleanColors.labelText,
                    contentColor = CleanColors.backgroundStart
                )
            ) {
                Text("Save")
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val epochMillis = state.date
            .atStartOfDay(ZoneId.of("UTC"))
            .toInstant()
            .toEpochMilli()
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = epochMillis)

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selected = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.of("UTC"))
                            .toLocalDate()
                        viewModel.updateDate(selected)
                    }
                    showDatePicker = false
                }) {
                    Text("OK", color = CleanColors.countdownText)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = CleanColors.labelText)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Time Picker Dialog (AlertDialog wrapping TimePicker)
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = state.time.hour,
            initialMinute = state.time.minute,
            is24Hour = false
        )

        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateTime(LocalTime.of(timePickerState.hour, timePickerState.minute))
                    showTimePicker = false
                }) {
                    Text("OK", color = CleanColors.countdownText)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel", color = CleanColors.labelText)
                }
            },
            text = {
                TimePicker(state = timePickerState)
            },
            containerColor = CleanColors.backgroundMid
        )
    }

    // Start Date Picker Dialog
    if (showStartDatePicker) {
        val epochMillis = state.startDate
            .atStartOfDay(ZoneId.of("UTC"))
            .toInstant()
            .toEpochMilli()
        val startDatePickerState = rememberDatePickerState(initialSelectedDateMillis = epochMillis)

        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    startDatePickerState.selectedDateMillis?.let { millis ->
                        val selected = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.of("UTC"))
                            .toLocalDate()
                        viewModel.updateStartDate(selected)
                    }
                    showStartDatePicker = false
                }) {
                    Text("OK", color = CleanColors.countdownText)
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text("Cancel", color = CleanColors.labelText)
                }
            }
        ) {
            DatePicker(state = startDatePickerState)
        }
    }

    // Start Time Picker Dialog
    if (showStartTimePicker) {
        val startTimePickerState = rememberTimePickerState(
            initialHour = state.startTime.hour,
            initialMinute = state.startTime.minute,
            is24Hour = false
        )

        AlertDialog(
            onDismissRequest = { showStartTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateStartTime(LocalTime.of(startTimePickerState.hour, startTimePickerState.minute))
                    showStartTimePicker = false
                }) {
                    Text("OK", color = CleanColors.countdownText)
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartTimePicker = false }) {
                    Text("Cancel", color = CleanColors.labelText)
                }
            },
            text = {
                TimePicker(state = startTimePickerState)
            },
            containerColor = CleanColors.backgroundMid
        )
    }
}
