package com.likeki.alquran.playback

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import com.likeki.alquran.AlQuranApp
import com.likeki.alquran.data.Track
import com.likeki.alquran.util.FileResolver

class PlayerController(private val context: Context) {

    private val app = context.applicationContext as AlQuranApp
    private var controller: MediaController? = null
    var onStateChanged: ((Boolean, Long, Long, Int) -> Unit)? = null
    var onError: ((String) -> Unit)? = null
    var onTrackEnded: (() -> Unit)? = null

    fun connect(onReady: () -> Unit) {
        val token = SessionToken(
            context,
            ComponentName(context, PlaybackService::class.java)
        )
        val future = MediaController.Builder(context, token).buildAsync()
        future.addListener({
            try {
                controller = future.get()
                controller?.addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        pushState()
                    }
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (playbackState == Player.STATE_ENDED) onTrackEnded?.invoke()
                        pushState()
                    }
                    override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                        onError?.invoke(error.message ?: "Playback error")
                    }
                    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                        pushState()
                    }
                })
                onReady()
            } catch (e: Exception) {
                onError?.invoke("Could not start player: ${e.message}")
            }
        }, MoreExecutors.directExecutor())
    }

    fun release() {
        controller?.release()
        controller = null
    }

    fun play(track: Track) {
        val c = controller ?: return
        if (!FileResolver.isDownloaded(app.audioDir, track.number, track.filename)) {
            onError?.invoke("Track ${track.number} is not downloaded yet. Download it first.")
            return
        }
        val uri = FileResolver.localUri(app.audioDir, track.number, track.filename)
        val item = MediaItem.fromUri(uri)
        c.setMediaItem(item)
        c.prepare()
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
