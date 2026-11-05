package com.likeki.alquran

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.likeki.alquran.data.Track
import com.likeki.alquran.playback.DownloadManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

data class DownloadState(
    val progress: Map<Int, Float> = emptyMap(),
    val downloading: Set<Int> = emptySet(),
    val error: String? = null
)

class DownloadViewModel(app: Application) : AndroidViewModel(app) {
    private val _state = MutableStateFlow(DownloadState())
    val state: StateFlow<DownloadState> = _state.asStateFlow()
    private val dm = DownloadManager(app)

    fun download(track: Track, baseUrl: String, onDone: (Int) -> Unit) {
        if (track.number in _state.value.downloading) return
        _state.value = _state.value.copy(
            downloading = _state.value.downloading + track.number,
            progress = _state.value.progress + (track.number to 0f),
            error = null
        )
        viewModelScope.launch {
            val result = dm.download(track, baseUrl) { p ->
                _state.value = _state.value.copy(
                    progress = _state.value.progress + (track.number to p)
                )
            }
            result.onSuccess {
                _state.value = _state.value.copy(
                    downloading = _state.value.downloading - track.number,
                    progress = _state.value.progress - track.number
                )
                onDone(track.number)
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    downloading = _state.value.downloading - track.number,
                    progress = _state.value.progress - track.number,
                    error = "Download failed for ${track.number}: ${e.message}"
                )
            }
        }
    }
}
