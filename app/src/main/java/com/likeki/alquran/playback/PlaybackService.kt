package com.likeki.alquran.playback

import android.content.Intent
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class PlaybackService : MediaSessionService() {
    private var session: MediaSession? = null
    override fun onCreate() {
        super.onCreate()
        val player = ExoPlayer.Builder(this)
            .setLoadControl(
                androidx.media3.exoplayer.DefaultLoadControl.Builder()
                    .setBufferDurationsMs(
                        15000,   // minBufferMs — 15 sec buffer target
                        60000,   // maxBufferMs — 60 sec max
                        1500,    // bufferForPlaybackMs — start after 1.5 sec
                        1000     // bufferForPlaybackAfterRebufferMs — 1 sec after rebuffer
                    )
                    .build()
            )
            .build()
        session = MediaSession.Builder(this, player).build()
    }
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = session
    override fun onTaskRemoved(rootIntent: Intent?) {
        val p = session?.player ?: return
        if (!p.playWhenReady || p.mediaItemCount == 0) stopSelf()
    }
    override fun onDestroy() {
        session?.run { player.release(); release(); session = null }
        super.onDestroy()
    }
}
