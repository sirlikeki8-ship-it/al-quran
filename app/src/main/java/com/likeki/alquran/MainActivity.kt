package com.likeki.alquran

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.likeki.alquran.data.SettingsStore
import com.likeki.alquran.playback.PlayerController
import com.likeki.alquran.ui.AboutScreen
import com.likeki.alquran.ui.NowPlayingBar
import com.likeki.alquran.ui.PlayerScreen
import com.likeki.alquran.ui.SettingsScreen
import com.likeki.alquran.ui.theme.AlQuranTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {

    private val vm: PlaybackViewModel by viewModels()
    private val settingsVm: SettingsViewModel by viewModels()
    private val downloadVm: DownloadViewModel by viewModels()
    private lateinit var controller: PlayerController
    private lateinit var appSettings: SettingsStore

    private val notifPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        appSettings = (application as AlQuranApp).settings
        controller = PlayerController(this)
        controller.connect { }
        maybeRequestNotifications()

        setContent {
            AlQuranTheme {
                AppRoot(
                    vm = vm,
                    settingsVm = settingsVm,
                    downloadVm = downloadVm,
                    controller = controller,
                    appSettings = appSettings
                )
            }
        }
    }

    private fun maybeRequestNotifications() {
        if (Build.VERSION.SDK_INT >= 33) {
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

@Composable
private fun AppRoot(
    vm: PlaybackViewModel,
    settingsVm: SettingsViewModel,
    downloadVm: DownloadViewModel,
    controller: PlayerController,
    appSettings: SettingsStore
) {
    var screen by remember { mutableStateOf("player") }
    val state by vm.state.collectAsState()
    val downloadState by downloadVm.state.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    DisposableEffect(Unit) {
        controller.onStateChanged = { playing, pos, dur, _ ->
            vm.setPlaying(playing)
            vm.setProgress(pos, dur)
        }
        controller.onError = { msg -> vm.setError(msg) }
        controller.onTrackEnded = {
            val next = nextIndex(state.currentIndex, state.repeatMode, state.shuffle, state.playlist.tracks.size)
            if (next != null) {
                vm.selectTrack(next)
                state.playlist.tracks.getOrNull(next)?.let { controller.play(it, state.playlist.baseUrl) }
            } else {
                vm.setPlaying(false)
            }
        }
        onDispose { controller.release() }
    }

    Column(Modifier.fillMaxSize()) {
        Box(Modifier.weight(1f)) {
            when (screen) {
                "player" -> PlayerScreen(
                    vm = vm,
                    onPlayPause = {
                        val track = state.currentTrack ?: return@PlayerScreen
                        if (!state.isPlaying) controller.play(track, state.playlist.baseUrl)
                        else controller.playPause()
                    },
                    onNext = {
                        val next = nextIndex(state.currentIndex, state.repeatMode, state.shuffle, state.playlist.tracks.size)
                        if (next != null) {
                            vm.selectTrack(next)
                            state.playlist.tracks.getOrNull(next)?.let { controller.play(it, state.playlist.baseUrl) }
                        }
                    },
                    onPrevious = {
                        val prev = (state.currentIndex - 1).takeIf { it >= 0 }
                            ?: (state.playlist.tracks.size - 1)
                        vm.selectTrack(prev)
                        state.playlist.tracks.getOrNull(prev)?.let { controller.play(it, state.playlist.baseUrl) }
                    },
                    onSeek = { ms -> controller.seekTo(ms) },
                    onSelectTrack = { idx ->
                        vm.selectTrack(idx)
                        state.playlist.tracks.getOrNull(idx)?.let { t ->
                            val resume = runBlocking {
                                if (appSettings.resumePosition.first()) appSettings.lastPosition.first() else 0L
                            }
                            controller.play(t, state.playlist.baseUrl, resume)
                        }
                    },
                    onDownload = { track, baseUrl ->
                        downloadVm.download(track, baseUrl) { num -> vm.markDownloaded(num) }
                    },
                    onDelete = { track ->
                        downloadVm.deleteTrack(track) { num -> vm.unmarkDownloaded(num) }
                    },
                    downloadingSet = downloadState.downloading,
                    progressMap = downloadState.progress,
                    baseUrl = state.playlist.baseUrl,
                    onSettings = { screen = "settings" }
                )

                "settings" -> {
                    LaunchedEffect(Unit) { settingsVm.refresh() }
                    SettingsScreen(
                        vm = settingsVm,
                        onBack = { screen = "player" },
                        onAbout = { screen = "about" },
                        onDownloadAll = {
                            downloadVm.downloadAll(state.playlist.tracks, state.playlist.baseUrl) { num ->
                                vm.markDownloaded(num)
                            }
                        },
                        onCancelAll = { downloadVm.cancelAll() }
                    )
                }

                "about" -> AboutScreen(onBack = { screen = "settings" })
            }
        }
        if (screen == "player") {
            NowPlayingBar(
                track = state.currentTrack,
                isPlaying = state.isPlaying,
                progress = if (state.durationMs > 0) state.positionMs.toFloat() / state.durationMs.toFloat() else 0f,
                onToggle = { controller.playPause() },
                onTap = { }
            )
        }
    }

    state.error?.let { msg ->
        LaunchedEffect(msg) {
            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
            vm.setError(null)
        }
    }
    downloadState.error?.let { msg ->
        LaunchedEffect(msg) {
            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
        }
    }
}

private fun nextIndex(current: Int, repeat: Int, shuffle: Boolean, total: Int): Int? {
    if (total == 0) return null
    if (repeat == 1) return current
    val cand = if (shuffle) {
        (0 until total).filter { it != current }.randomOrNull()
    } else current + 1
    return when {
        cand == null -> null
        cand < total -> cand
        repeat == 2 -> 0
        else -> null
    }
}
