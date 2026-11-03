package com.likeki.alquran

import android.app.Application
import com.likeki.alquran.data.PlaylistRepository
import com.likeki.alquran.data.SettingsStore
import java.io.File

class AlQuranApp : Application() {
    val audioDir: File by lazy {
        File(getExternalFilesDir(null) ?: filesDir, "audio").apply { mkdirs() }
    }
    val settings: SettingsStore by lazy { SettingsStore(this) }
    val playlistRepo: PlaylistRepository by lazy { PlaylistRepository(this) }
}
