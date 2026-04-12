package com.dreslan.countdown.ui.list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.dreslan.countdown.data.CountdownDatabase
import kotlinx.coroutines.flow.Flow
import com.dreslan.countdown.data.Countdown

class CountdownListViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = CountdownDatabase.getInstance(application).countdownDao()
    val countdowns: Flow<List<Countdown>> = dao.getAll()
}
