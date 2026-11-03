package com.likeki.alquran.util

import android.net.Uri
import java.io.File
import java.net.URLEncoder

object FileResolver {

    fun localFile(audioDir: File, trackNumber: Int, filename: String): File =
        File(audioDir, "${trackNumber}_$filename")

    fun isDownloaded(audioDir: File, trackNumber: Int, filename: String): Boolean {
        val f = localFile(audioDir, trackNumber, filename)
        return f.exists() && f.length() > 0
    }

    fun remoteUrl(baseUrl: String, filename: String): String {
        val base = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        val encoded = filename.split("/").joinToString("/") { seg ->
            URLEncoder.encode(seg, "UTF-8").replace("+", "%20")
        }
        return base + encoded
    }

    fun localUri(audioDir: File, trackNumber: Int, filename: String): Uri =
        Uri.fromFile(localFile(audioDir, trackNumber, filename))
}
