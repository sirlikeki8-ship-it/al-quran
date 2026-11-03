package com.likeki.alquran

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class SettingsUiState(
    val shuffle: Boolean = false,
    val repeatMode: Int = 0,
    val autoPlayNext: Boolean = true,
    val resumePosition: Boolean = true,
    val wifiOnly: Boolean = false,
    val themeMode: Int = 0,
    val textSize: Int = 1,
    val showArabic: Boolean = true,
    val storageBytes: Long = 0L,
    val totalTracks: Int = 0,
    val downloadedTracks: Int = 0,
    val missingTracks: Int = 0
)

class SettingsViewModel(app: Application) : AndroidViewModel(app) {
    private val alApp = app as AlQuranApp
    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            val s = alApp.settings
            val playlist = alApp.playlistRepo.load()
            val downloaded = playlist.tracks.count {
                com.likeki.alquran.util.FileResolver.isDownloaded(
                    alApp.audioDir, it.number, it.filename
                )
            }
            val bytes = alApp.audioDir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
            _state.value = SettingsUiState(
                shuffle = s.shuffle.first(),
                repeatMode = s.repeatMode.first(),
                autoPlayNext = s.autoPlayNext.first(),
                resumePosition = s.resumePosition.first(),
                wifiOnly = s.wifiOnly.first(),
                themeMode = s.themeMode.first(),
                textSize = s.textSize.first(),
                showArabic = s.showArabic.first(),
                storageBytes = bytes,
                totalTracks = playlist.tracks.size,
                downloadedTracks = downloaded,
                missingTracks = (115 - playlist.tracks.size).coerceAtLeast(0)
            )
        }
    }

    fun toggleShuffle() = viewModelScope.launch {
        val next = !_state.value.shuffle
        alApp.settings.setShuffle(next); _state.value = _state.value.copy(shuffle = next)
    }
    fun cycleRepeat() = viewModelScope.launch {
        val next = (_state.value.repeatMode + 1) % 3
        alApp.settings.setRepeatMode(next); _state.value = _state.value.copy(repeatMode = next)
    }
    fun toggleAutoPlayNext() = viewModelScope.launch {
        val next = !_state.value.autoPlayNext
        alApp.settings.setAutoPlayNext(next); _state.value = _state.value.copy(autoPlayNext = next)
    }
    fun toggleResumePosition() = viewModelScope.launch {
        val next = !_state.value.resumePosition
        alApp.settings.setResumePosition(next); _state.value = _state.value.copy(resumePosition = next)
    }
    fun toggleWifiOnly() = viewModelScope.launch {
        val next = !_state.value.wifiOnly
        alApp.settings.setWifiOnly(next); _state.value = _state.value.copy(wifiOnly = next)
    }
    fun cycleTheme() = viewModelScope.launch {
        val next = (_state.value.themeMode + 1) % 3
        alApp.settings.setThemeMode(next); _state.value = _state.value.copy(themeMode = next)
    }
    fun cycleTextSize() = viewModelScope.launch {
        val next = (_state.value.textSize + 1) % 3
        alApp.settings.setTextSize(next); _state.value = _state.value.copy(textSize = next)
    }
    fun toggleShowArabic() = viewModelScope.launch {
        val next = !_state.value.showArabic
        alApp.settings.setShowArabic(next); _state.value = _state.value.copy(showArabic = next)
    }
    fun clearDownloads() = viewModelScope.launch {
        alApp.audioDir.listFiles()?.forEach { it.delete() }
        refresh()
    }
    fun resetSettings() = viewModelScope.launch {
        alApp.settings.resetAll(); refresh()
    }
}
