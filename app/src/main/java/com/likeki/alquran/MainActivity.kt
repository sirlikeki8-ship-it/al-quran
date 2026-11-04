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
import androidx.compose.runtime.*
import kotlinx.coroutines.flow.first
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.likeki.alquran.playback.PlayerController
import com.likeki.alquran.ui.AboutScreen
import com.likeki.alquran.ui.PlayerScreen
import com.likeki.alquran.ui.SettingsScreen
import com.likeki.alquran.ui.theme.AlQuranTheme

class MainActivity : ComponentActivity() {

    private val vm: PlaybackViewModel by viewModels()
    private val settingsVm: SettingsViewModel by viewModels()
    private lateinit var controller: PlayerController

    private val notifPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        controller = PlayerController(this)
        controller.connect { }

        val settings = (application as AlQuranApp).settings

        maybeRequestNotifications()

        setContent {
            AlQuranTheme {
                var screen by remember { mutableStateOf("player") }
                val state by vm.state.collectAsState()

                DisposableEffect(Unit) {
                    controller.onStateChanged = { playing, pos, dur, _ ->
                        vm.setPlaying(playing)
                        vm.setProgress(pos, dur)
                    }
                    controller.onError = { msg -> vm.setError(msg) }
                    controller.onTrackEnded = {
                        val autoNext = kotlinx.coroutines.runBlocking { settings.autoPlayNext.first() }
                        if (!autoNext) {
                            vm.setPlaying(false)
                            return@onTrackEnded
                        }
                        val next = nextIndex(state.currentIndex, state.repeatMode, state.shuffle, state.playlist.tracks.size)
                        if (next != null) {
                            vm.selectTrack(next)
                            state.playlist.tracks.getOrNull(next)?.let { controller.play(it) }
                        } else {
                            vm.setPlaying(false)
                        }
                    }
                    onDispose { controller.release() }
                }

                when (screen) {
                    "player" -> PlayerScreen(
                        vm = vm,
                        onPlayPause = {
                            val track = state.currentTrack ?: return@PlayerScreen
                            if (!state.isPlaying) controller.play(track) else controller.playPause()
                        },
                        onNext = {
                            val next = nextIndex(state.currentIndex, state.repeatMode, state.shuffle, state.playlist.tracks.size)
                            if (next != null) {
                                vm.selectTrack(next)
                                state.playlist.tracks.getOrNull(next)?.let { controller.play(it) }
                            }
                        },
                        onPrevious = {
                            val prev = (state.currentIndex - 1).takeIf { it >= 0 }
                                ?: (state.playlist.tracks.size - 1)
                            vm.selectTrack(prev)
                            state.playlist.tracks.getOrNull(prev)?.let { controller.play(it) }
                        },
                        onSeek = { ms -> controller.seekTo(ms) },
                        onSelectTrack = { idx ->
                            vm.selectTrack(idx)
                            state.playlist.tracks.getOrNull(idx)?.let { controller.play(it) }
                        },
                        onSettings = { screen = "settings" }
                    )

                    "settings" -> {
                        LaunchedEffect(Unit) { settingsVm.refresh() }
                        SettingsScreen(
                            vm = settingsVm,
                            onBack = { screen = "player" },
                            onAbout = { screen = "about" }
                        )
                    }

                    "about" -> AboutScreen(onBack = { screen = "settings" })
                }

                state.error?.let { msg ->
                    LaunchedEffect(msg) {
                        android.widget.Toast.makeText(this@MainActivity, msg, android.widget.Toast.LENGTH_LONG).show()
                        vm.setError(null)
                    }
                }
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
}
