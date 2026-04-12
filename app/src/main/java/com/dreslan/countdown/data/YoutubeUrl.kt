package com.dreslan.countdown.data

private val YOUTUBE_PATTERNS = listOf(
    Regex("""(?:https?://)?(?:www\.)?youtube\.com/watch\?v=([a-zA-Z0-9_-]+)"""),
    Regex("""(?:https?://)?youtu\.be/([a-zA-Z0-9_-]+)"""),
    Regex("""(?:https?://)?(?:www\.)?youtube\.com/shorts/([a-zA-Z0-9_-]+)"""),
    Regex("""(?:https?://)?(?:www\.)?youtube\.com/embed/([a-zA-Z0-9_-]+)"""),
)

fun normalizeYoutubeUrl(url: String): String? {
    for (pattern in YOUTUBE_PATTERNS) {
        val match = pattern.find(url)
        if (match != null) {
            return "https://www.youtube.com/embed/${match.groupValues[1]}"
        }
    }
    return null
}
