package com.likeki.alquran.playback

import android.content.Context
import com.likeki.alquran.AlQuranApp
import com.likeki.alquran.data.Track
import com.likeki.alquran.util.FileResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class DownloadManager(private val context: Context) {
    private val app = context.applicationContext as AlQuranApp

    suspend fun download(track: Track, baseUrl: String, onProgress: (Float) -> Unit): Result<File> =
        withContext(Dispatchers.IO) {
            try {
                val target = FileResolver.localFile(app.audioDir, track.number, track.filename)
                if (target.exists() && target.length() > 0) {
                    onProgress(1f)
                    return@withContext Result.success(target)
                }
                val partial = File(target.parentFile, target.name + ".part")
                val url = URL(FileResolver.remoteUrl(baseUrl, track.filename))
                val conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = 15000
                conn.readTimeout = 30000
                conn.instanceFollowRedirects = true
                conn.connect()
                if (conn.responseCode !in 200..299) {
                    return@withContext Result.failure(Exception("HTTP ${conn.responseCode} for ${track.filename}"))
                }
                val total = conn.contentLengthLong
                conn.inputStream.use { input ->
                    partial.outputStream().use { output ->
                        val buf = ByteArray(64 * 1024)
                        var read: Int
                        var done = 0L
                        while (input.read(buf).also { read = it } > 0) {
                            output.write(buf, 0, read)
                            done += read
                            if (total > 0) onProgress((done.toFloat() / total).coerceIn(0f, 1f))
                        }
                    }
                }
                conn.disconnect()
                if (partial.renameTo(target)) {
                    onProgress(1f)
                    Result.success(target)
                } else {
                    Result.failure(Exception("Could not finalize file"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
