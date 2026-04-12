package com.dreslan.countdown.data

import android.content.Context
import android.net.Uri
import java.io.File
import java.util.UUID

fun saveImageToInternal(context: Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val fileName = "countdown_bg_${UUID.randomUUID()}.jpg"
        val file = File(context.filesDir, fileName)
        file.outputStream().use { output ->
            inputStream.copyTo(output)
        }
        inputStream.close()
        file.absolutePath
    } catch (e: Exception) {
        null
    }
}

fun deleteImage(path: String) {
    try {
        File(path).delete()
    } catch (_: Exception) { }
}
