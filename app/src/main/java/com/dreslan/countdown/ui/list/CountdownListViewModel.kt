package com.dreslan.countdown.ui.list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.dreslan.countdown.data.CountdownDatabase
import com.dreslan.countdown.data.Countdown
import kotlinx.coroutines.flow.Flow

class CountdownListViewModel(application: Application) : AndroidViewModel(application) {
    private val db = CountdownDatabase.getInstance(application)
    val countdowns: Flow<List<Countdown>> = db.countdownDao().getAll()

    fun getNoteCount(countdownId: Long): Flow<Int> = db.noteDao().getNoteCountForCountdown(countdownId)
}
