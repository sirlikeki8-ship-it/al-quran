package com.likeki.alquran.playback

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import com.likeki.alquran.AlQuranApp
import com.likeki.alquran.data.Track
import com.likeki.alquran.util.FileResolver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PlayerController(private val context: Context) {
    private val app = context.applicationContext as AlQuranApp
    private var controller: MediaController? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var pollJob: Job? = null
    var onStateChanged: ((Boolean, Long, Long, Int) -> Unit)? = null
    var onError: ((String) -> Unit)? = null
    var onTrackEnded: (() -> Unit)? = null

    fun connect(onReady: () -> Unit) {
        val token = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val future = MediaController.Builder(context, token).buildAsync()
        future.addListener({
            try {
                controller = future.get()
                controller?.addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) { pushState() }
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (playbackState == Player.STATE_ENDED) onTrackEnded?.invoke()
                        pushState()
                    }
                    override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                        onError?.invoke(error.message ?: "Playback error")
                    }
                    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) { pushState() }
                })
                startPolling()
                onReady()
            } catch (e: Exception) {
                onError?.invoke("Could not start player: ${e.message}")
            }
        }, MoreExecutors.directExecutor())
    }

    fun release() {
        pollJob?.cancel()
        scope.cancel()
        controller?.release()
        controller = null
    }

    private fun startPolling() {
        pollJob?.cancel()
        pollJob = scope.launch {
            while (isActive) {
                pushState()
                delay(500)
            }
        }
    }

    fun play(track: Track, baseUrl: String, resumeMs: Long = 0L) {
        val c = controller ?: return
        val localFile = FileResolver.localFile(app.audioDir, track.number, track.filename)
        val uri: Uri = if (localFile.exists() && localFile.length() > 0) {
            Uri.fromFile(localFile)
        } else {
            Uri.parse(FileResolver.remoteUrl(baseUrl, track.filename))
        }
        c.setMediaItem(MediaItem.fromUri(uri))
        c.prepare()
        if (resumeMs > 0) c.seekTo(resumeMs)
        c.play()
    }

    fun playPause() {
        val c = controller ?: return
        if (c.isPlaying) c.pause() else {
            if (c.playbackState == Player.STATE_IDLE) c.prepare()
            c.play()
        }
    }
    fun seekTo(ms: Long) { controller?.seekTo(ms) }
    fun setRepeatMode(mode: Int) {
        controller?.repeatMode = when (mode) {
            1 -> Player.REPEAT_MODE_ONE
            2 -> Player.REPEAT_MODE_ALL
            else -> Player.REPEAT_MODE_OFF
        }
    }
    fun setShuffle(on: Boolean) { controller?.shuffleModeEnabled = on }
    private fun pushState() {
        val c = controller ?: return
        onStateChanged?.invoke(
            c.isPlaying,
            c.currentPosition.coerceAtLeast(0),
            c.duration.coerceAtLeast(0),
            c.currentMediaItemIndex
        )
    }
}
