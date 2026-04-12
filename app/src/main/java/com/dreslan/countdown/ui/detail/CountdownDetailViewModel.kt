package com.dreslan.countdown.ui.detail

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dreslan.countdown.data.Countdown
import com.dreslan.countdown.data.CountdownDatabase
import com.dreslan.countdown.data.Note
import com.dreslan.countdown.data.ExportFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.Instant

data class DetailState(
    val countdown: Countdown? = null,
    val notes: List<Note> = emptyList(),
    val isDeleted: Boolean = false
)

class CountdownDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val db = CountdownDatabase.getInstance(application)
    private val countdownDao = db.countdownDao()
    private val noteDao = db.noteDao()
    private val _state = MutableStateFlow(DetailState())
    val state: StateFlow<DetailState> = _state

    fun loadCountdown(id: Long) {
        viewModelScope.launch {
            countdownDao.getByIdFlow(id).collect { countdown ->
                if (!_state.value.isDeleted) {
                    _state.value = _state.value.copy(countdown = countdown)
                }
            }
        }
        viewModelScope.launch {
            noteDao.getNotesForCountdown(id).collect { notes ->
                _state.value = _state.value.copy(notes = notes)
            }
        }
    }

    fun addNote(text: String, createdAt: Instant) {
        val countdown = _state.value.countdown ?: return
        viewModelScope.launch {
            noteDao.insert(
                Note(countdownId = countdown.id, text = text, createdAt = createdAt)
            )
        }
    }

    fun updateNote(note: Note, text: String, createdAt: Instant) {
        viewModelScope.launch {
            noteDao.update(
                note.copy(text = text, createdAt = createdAt, updatedAt = Instant.now())
            )
        }
    }

    fun deleteNote(noteId: Long) {
        viewModelScope.launch {
            noteDao.deleteById(noteId)
        }
    }

    fun deleteCountdown() {
        viewModelScope.launch {
            val countdown = _state.value.countdown ?: return@launch
            _state.value = _state.value.copy(isDeleted = true)
            countdownDao.delete(countdown)
        }
    }

    fun exportMarkdown(): Intent {
        val s = _state.value
        val text = ExportFormatter.toMarkdown(s.countdown!!, s.notes)
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_SUBJECT, s.countdown.title)
        }
    }

    fun exportJson(): Intent {
        val s = _state.value
        val text = ExportFormatter.toJson(s.countdown!!, s.notes)
        return Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_SUBJECT, s.countdown.title)
        }
    }
}
