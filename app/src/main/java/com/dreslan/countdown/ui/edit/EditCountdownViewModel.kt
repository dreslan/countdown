package com.dreslan.countdown.ui.edit

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dreslan.countdown.data.Countdown
import com.dreslan.countdown.data.CountdownDatabase
import com.dreslan.countdown.data.CountdownTheme
import com.dreslan.countdown.data.normalizeYoutubeUrl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

data class EditState(
    val title: String = "",
    val date: LocalDate = LocalDate.now().plusDays(1),
    val time: LocalTime = LocalTime.NOON,
    val timeZone: ZoneId = ZoneId.systemDefault(),
    val theme: CountdownTheme = CountdownTheme.CLEAN,
    val zeroMessage: String = "",
    val videoUrl: String = "",
    val showProgress: Boolean = false,
    val isEditing: Boolean = false,
    val editId: Long = 0,
    val videoUrlError: String? = null,
    val titleError: String? = null,
    val isSaved: Boolean = false,
)

class EditCountdownViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = CountdownDatabase.getInstance(application).countdownDao()
    private val _state = MutableStateFlow(EditState())
    val state: StateFlow<EditState> = _state

    fun loadCountdown(id: Long) {
        viewModelScope.launch {
            val countdown = dao.getById(id) ?: return@launch
            val zone = ZoneId.of(countdown.timeZone)
            val zdt = countdown.targetDateTime.atZone(zone)
            _state.value = EditState(
                title = countdown.title,
                date = zdt.toLocalDate(),
                time = zdt.toLocalTime(),
                timeZone = zone,
                theme = countdown.theme,
                zeroMessage = countdown.zeroMessage ?: "",
                videoUrl = countdown.videoUrl ?: "",
                showProgress = countdown.showProgress,
                isEditing = true,
                editId = countdown.id
            )
        }
    }

    fun updateTitle(title: String) {
        _state.value = _state.value.copy(title = title, titleError = null)
    }

    fun updateDate(date: LocalDate) {
        _state.value = _state.value.copy(date = date)
    }

    fun updateTime(time: LocalTime) {
        _state.value = _state.value.copy(time = time)
    }

    fun updateTimeZone(zone: ZoneId) {
        _state.value = _state.value.copy(timeZone = zone)
    }

    fun updateTheme(theme: CountdownTheme) {
        _state.value = _state.value.copy(theme = theme)
    }

    fun updateZeroMessage(message: String) {
        _state.value = _state.value.copy(zeroMessage = message)
    }

    fun updateVideoUrl(url: String) {
        _state.value = _state.value.copy(videoUrl = url, videoUrlError = null)
    }

    fun updateShowProgress(show: Boolean) {
        _state.value = _state.value.copy(showProgress = show)
    }

    fun save() {
        val s = _state.value
        if (s.title.isBlank()) {
            _state.value = s.copy(titleError = "Title is required")
            return
        }

        val normalizedVideoUrl = if (s.videoUrl.isBlank()) {
            null
        } else {
            val normalized = normalizeYoutubeUrl(s.videoUrl)
            if (normalized == null) {
                _state.value = s.copy(videoUrlError = "Not a valid YouTube URL")
                return
            }
            normalized
        }

        val targetInstant = s.date.atTime(s.time)
            .atZone(s.timeZone)
            .toInstant()

        viewModelScope.launch {
            if (s.isEditing) {
                val existing = dao.getById(s.editId) ?: return@launch
                dao.update(
                    existing.copy(
                        title = s.title.trim(),
                        targetDateTime = targetInstant,
                        timeZone = s.timeZone.id,
                        theme = s.theme,
                        zeroMessage = s.zeroMessage.trim().ifBlank { null },
                        videoUrl = normalizedVideoUrl,
                        showProgress = s.showProgress
                    )
                )
            } else {
                dao.insert(
                    Countdown(
                        title = s.title.trim(),
                        targetDateTime = targetInstant,
                        timeZone = s.timeZone.id,
                        theme = s.theme,
                        zeroMessage = s.zeroMessage.trim().ifBlank { null },
                        videoUrl = normalizedVideoUrl,
                        showProgress = s.showProgress
                    )
                )
            }
            _state.value = _state.value.copy(isSaved = true)
        }
    }
}
