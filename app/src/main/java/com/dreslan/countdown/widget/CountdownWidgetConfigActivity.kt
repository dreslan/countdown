package com.dreslan.countdown.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.lifecycle.lifecycleScope
import com.dreslan.countdown.MainActivity
import com.dreslan.countdown.data.Countdown
import com.dreslan.countdown.data.CountdownDatabase
import com.dreslan.countdown.ui.theme.CleanColors
import com.dreslan.countdown.ui.theme.CountdownAppTheme
import kotlinx.coroutines.launch

class CountdownWidgetConfigActivity : ComponentActivity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val dao = CountdownDatabase.getInstance(this).countdownDao()

        setContent {
            CountdownAppTheme {
                val countdowns by dao.getAll().collectAsState(initial = emptyList())
                ConfigScreen(
                    countdowns = countdowns,
                    onSelect = { countdown -> selectCountdown(countdown.id) },
                    onCreateClick = {
                        startActivity(Intent(this@CountdownWidgetConfigActivity, MainActivity::class.java))
                    }
                )
            }
        }
    }

    private fun selectCountdown(countdownId: Long) {
        saveWidgetCountdownId(this, appWidgetId, countdownId)

        lifecycleScope.launch {
            val manager = GlanceAppWidgetManager(this@CountdownWidgetConfigActivity)
            try {
                val glanceId = manager.getGlanceIdBy(appWidgetId)
                CountdownWidget().update(this@CountdownWidgetConfigActivity, glanceId)
            } catch (_: Exception) {
                try {
                    val glanceId = manager.getGlanceIdBy(appWidgetId)
                    CountdownWidgetSmall().update(this@CountdownWidgetConfigActivity, glanceId)
                } catch (_: Exception) { }
            }
        }

        val result = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_OK, result)
        finish()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfigScreen(
    countdowns: List<Countdown>,
    onSelect: (Countdown) -> Unit,
    onCreateClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Countdown") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CleanColors.backgroundStart,
                    titleContentColor = CleanColors.countdownText
                )
            )
        },
        containerColor = CleanColors.backgroundStart
    ) { padding ->
        if (countdowns.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No countdowns yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = CleanColors.labelText
                    )
                    Button(
                        onClick = onCreateClick,
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text("Create One")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(countdowns, key = { it.id }) { countdown ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onSelect(countdown) },
                        colors = CardDefaults.cardColors(containerColor = CleanColors.backgroundMid)
                    ) {
                        Text(
                            text = countdown.title,
                            style = MaterialTheme.typography.bodyLarge,
                            color = CleanColors.countdownText,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}
