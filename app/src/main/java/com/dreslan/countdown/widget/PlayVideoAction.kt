package com.dreslan.countdown.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback

class PlayVideoAction : ActionCallback {
    companion object {
        val VIDEO_URL_KEY = ActionParameters.Key<String>("video_url")
    }

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val embedUrl = parameters[VIDEO_URL_KEY] ?: return
        val videoId = embedUrl.substringAfterLast("/").substringBefore("?")
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$videoId"))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
