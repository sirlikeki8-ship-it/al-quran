package com.likeki.alquran

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.likeki.alquran.data.Playlist
import com.likeki.alquran.data.Track
import com.likeki.alquran.util.FileResolver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class UiState(
    val playlist: Playlist = Playlist(),
    val currentIndex: Int = 0,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val shuffle: Boolean = false,
    val repeatMode: Int = 0,
    val search: String = "",
    val downloadedSet: Set<Int> = emptySet(),
    val error: String? = null
) {
    val currentTrack: Track? get() = playlist.tracks.getOrNull(currentIndex)
    val filteredTracks: List<Track>
        get() = if (search.isBlank()) playlist.tracks else playlist.tracks.filter {
            it.number.toString().contains(search, ignoreCase = true) ||
                it.displayName.contains(search, ignoreCase = true) ||
                it.displayNameArabic.contains(search, ignoreCase = true)
        }
}

class PlaybackViewModel(app: Application) : AndroidViewModel(app) {
    private val alApp = app as AlQuranApp
    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val playlist = alApp.playlistRepo.load()
            val downloaded = playlist.tracks.filter {
                FileResolver.isDownloaded(alApp.audioDir, it.number, it.filename)
            }.map { it.number }.toSet()
            val lt = alApp.settings.lastTrack.first()
            val sh = alApp.settings.shuffle.first()
            val rp = alApp.settings.repeatMode.first()
            _state.value = _state.value.copy(
                playlist = playlist,
                currentIndex = lt.coerceIn(0, (playlist.tracks.size - 1).coerceAtLeast(0)),
                downloadedSet = downloaded,
                shuffle = sh,
                repeatMode = rp
            )
        }
    }

    fun setSearch(q: String) { _state.value = _state.value.copy(search = q) }
    fun selectTrack(index: Int) { _state.value = _state.value.copy(currentIndex = index) }
    fun setPlaying(p: Boolean) { _state.value = _state.value.copy(isPlaying = p) }
    fun setProgress(pos: Long, dur: Long) {
        _state.value = _state.value.copy(positionMs = pos, durationMs = dur)
    }
    fun toggleShuffle() {
        val next = !_state.value.shuffle
        _state.value = _state.value.copy(shuffle = next)
        viewModelScope.launch { alApp.settings.setShuffle(next) }
    }
    fun cycleRepeat() {
        val next = (_state.value.repeatMode + 1) % 3
        _state.value = _state.value.copy(repeatMode = next)
        viewModelScope.launch { alApp.settings.setRepeatMode(next) }
    }
    fun markDownloaded(number: Int) {
        _state.value = _state.value.copy(downloadedSet = _state.value.downloadedSet + number)
    }
    fun setError(msg: String?) { _state.value = _state.value.copy(error = msg) }
    fun unmarkDownloaded(number: Int) {
        _state.value = _state.value.copy(downloadedSet = _state.value.downloadedSet - number)
    }
}
