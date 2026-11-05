package com.likeki.alquran

import android.app.Application
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.likeki.alquran.data.Track
import com.likeki.alquran.playback.DownloadManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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
    private val alApp = app as AlQuranApp

    private fun isOnWifi(): Boolean {
        val cm = getApplication<Application>().getSystemService(ConnectivityManager::class.java)
        val net = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(net) ?: return false
        return caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    fun download(track: Track, baseUrl: String, onDone: (Int) -> Unit) {
        if (track.number in _state.value.downloading) return
        viewModelScope.launch {
            val wifiOnly = alApp.settings.wifiOnly.first()
            if (wifiOnly && !isOnWifi()) {
                _state.value = _state.value.copy(error = "Wi-Fi only enabled. Connect to Wi-Fi to download.")
                return@launch
            }
            _state.value = _state.value.copy(
                downloading = _state.value.downloading + track.number,
                progress = _state.value.progress + (track.number to 0f),
                error = null
            )
            val result = dm.download(track, baseUrl) { p ->
                _state.value = _state.value.copy(progress = _state.value.progress + (track.number to p))
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

    fun downloadAll(tracks: List<Track>, baseUrl: String, onEachDone: (Int) -> Unit) {
        viewModelScope.launch {
            val wifiOnly = alApp.settings.wifiOnly.first()
            if (wifiOnly && !isOnWifi()) {
                _state.value = _state.value.copy(error = "Wi-Fi only enabled. Connect to Wi-Fi to download.")
                return@launch
            }
            for (t in tracks) {
                if (com.likeki.alquran.util.FileResolver.isDownloaded(alApp.audioDir, t.number, t.filename)) continue
                _state.value = _state.value.copy(
                    downloading = _state.value.downloading + t.number,
                    progress = _state.value.progress + (t.number to 0f)
                )
                val result = dm.download(t, baseUrl) { p ->
                    _state.value = _state.value.copy(progress = _state.value.progress + (t.number to p))
                }
                result.onSuccess {
                    _state.value = _state.value.copy(
                        downloading = _state.value.downloading - t.number,
                        progress = _state.value.progress - t.number
                    )
                    onEachDone(t.number)
                }.onFailure { e ->
                    _state.value = _state.value.copy(
                        downloading = _state.value.downloading - t.number,
                        progress = _state.value.progress - t.number,
                        error = "Failed ${t.number}: ${e.message}"
                    )
                }
            }
        }
    }

    fun cancelAll() {
        dm.cancel()
        _state.value = _state.value.copy(downloading = emptySet(), progress = emptyMap())
        viewModelScope.launch { kotlinx.coroutines.delay(500); dm.resetCancel() }
    }

    fun deleteTrack(track: Track, onDeleted: (Int) -> Unit) {
        val f = com.likeki.alquran.util.FileResolver.localFile(alApp.audioDir, track.number, track.filename)
        if (f.exists()) f.delete()
        onDeleted(track.number)
    }
}
