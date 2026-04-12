package com.dreslan.countdown

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dreslan.countdown.ui.detail.CountdownDetailScreen
import com.dreslan.countdown.ui.edit.EditCountdownScreen
import com.dreslan.countdown.ui.list.CountdownListScreen
import com.dreslan.countdown.ui.theme.CountdownAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CountdownAppTheme {
                CountdownNavHost()
            }
        }
    }
}

@Composable
private fun CountdownNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "list") {
        composable("list") {
            CountdownListScreen(
                onCountdownClick = { id -> navController.navigate("detail/$id") },
                onCreateClick = { navController.navigate("edit/0") }
            )
        }

        composable(
            "detail/{countdownId}?autoPlay={autoPlay}",
            arguments = listOf(
                navArgument("countdownId") { type = NavType.LongType },
                navArgument("autoPlay") { type = NavType.BoolType; defaultValue = false }
            )
        ) { backStackEntry ->
            val countdownId = backStackEntry.arguments?.getLong("countdownId") ?: return@composable
            val autoPlay = backStackEntry.arguments?.getBoolean("autoPlay") ?: false
            CountdownDetailScreen(
                countdownId = countdownId,
                autoPlayVideo = autoPlay,
                onNavigateBack = { navController.popBackStack() },
                onEditClick = { id -> navController.navigate("edit/$id") }
            )
        }

        composable(
            "edit/{countdownId}",
            arguments = listOf(
                navArgument("countdownId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val countdownId = backStackEntry.arguments?.getLong("countdownId") ?: 0L
            EditCountdownScreen(
                countdownId = if (countdownId > 0) countdownId else null,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
