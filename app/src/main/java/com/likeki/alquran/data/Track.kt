package com.likeki.alquran.data

import kotlinx.serialization.Serializable

@Serializable
data class Track(
    val number: Int,
    val displayName: String,
    val displayNameArabic: String = "",
    val filename: String,
    val durationMs: Long = 0L
)

@Serializable
data class Playlist(
    val version: Int = 1,
    val baseUrl: String = "",
    val tracks: List<Track> = emptyList()
)
