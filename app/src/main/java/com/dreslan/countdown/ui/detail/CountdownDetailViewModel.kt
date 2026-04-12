package com.dreslan.countdown.ui.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dreslan.countdown.data.Countdown
import com.dreslan.countdown.data.CountdownDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class DetailState(
    val countdown: Countdown? = null,
    val isDeleted: Boolean = false
)

class CountdownDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = CountdownDatabase.getInstance(application).countdownDao()
    private val _state = MutableStateFlow(DetailState())
    val state: StateFlow<DetailState> = _state

    fun loadCountdown(id: Long) {
        viewModelScope.launch {
            _state.value = DetailState(countdown = dao.getById(id))
        }
    }

    fun deleteCountdown() {
        viewModelScope.launch {
            val countdown = _state.value.countdown ?: return@launch
            dao.delete(countdown)
            _state.value = _state.value.copy(isDeleted = true)
        }
    }
}
