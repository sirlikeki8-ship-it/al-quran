package com.likeki.alquran.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class PlaylistRepository(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun load(): Playlist = withContext(Dispatchers.IO) {
        try {
            val raw = context.assets.open("playlist.json").bufferedReader().use { it.readText() }
            json.decodeFromString<Playlist>(raw)
        } catch (e: Exception) {
            Playlist()
        }
    }
}
