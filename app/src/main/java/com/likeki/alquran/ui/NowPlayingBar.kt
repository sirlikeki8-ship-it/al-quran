package com.likeki.alquran.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.likeki.alquran.data.Track

@Composable
fun NowPlayingBar(
    track: Track?,
    isPlaying: Boolean,
    progress: Float,
    onToggle: () -> Unit,
    onTap: () -> Unit
) {
    if (track == null) return
    Surface(
        tonalElevation = 6.dp,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(2.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onTap() }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "${track.number.toString().padStart(3, '0')} · ${track.displayNameArabic.ifBlank { track.displayName }}",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        maxLines = 1
                    )
                    Text(track.displayName, fontSize = 11.sp, maxLines = 1)
                }
                IconButton(onClick = onToggle) {
                    Icon(
                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        "Play/Pause"
                    )
                }
            }
        }
    }
}
