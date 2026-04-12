package com.dreslan.countdown.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class YoutubeUrlTest {
    @Test
    fun normalizes_watch_url() {
        assertEquals(
            "https://www.youtube.com/embed/lLCEUpIg8rE",
            normalizeYoutubeUrl("https://www.youtube.com/watch?v=lLCEUpIg8rE")
        )
    }

    @Test
    fun normalizes_short_url() {
        assertEquals(
            "https://www.youtube.com/embed/lLCEUpIg8rE",
            normalizeYoutubeUrl("https://youtu.be/lLCEUpIg8rE")
        )
    }

    @Test
    fun normalizes_shorts_url() {
        assertEquals(
            "https://www.youtube.com/embed/lLCEUpIg8rE",
            normalizeYoutubeUrl("https://www.youtube.com/shorts/lLCEUpIg8rE")
        )
    }

    @Test
    fun passes_through_embed_url() {
        assertEquals(
            "https://www.youtube.com/embed/lLCEUpIg8rE",
            normalizeYoutubeUrl("https://www.youtube.com/embed/lLCEUpIg8rE")
        )
    }

    @Test
    fun handles_url_without_https() {
        assertEquals(
            "https://www.youtube.com/embed/lLCEUpIg8rE",
            normalizeYoutubeUrl("youtube.com/watch?v=lLCEUpIg8rE")
        )
    }

    @Test
    fun handles_watch_url_with_extra_params() {
        assertEquals(
            "https://www.youtube.com/embed/lLCEUpIg8rE",
            normalizeYoutubeUrl("https://www.youtube.com/watch?v=lLCEUpIg8rE&t=120")
        )
    }

    @Test
    fun returns_null_for_non_youtube_url() {
        assertNull(normalizeYoutubeUrl("https://vimeo.com/123456"))
    }

    @Test
    fun returns_null_for_garbage_input() {
        assertNull(normalizeYoutubeUrl("not a url"))
    }

    @Test
    fun returns_null_for_empty_string() {
        assertNull(normalizeYoutubeUrl(""))
    }
}
