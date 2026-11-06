package com.likeki.alquran.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.likeki.alquran.PlaybackViewModel
import com.likeki.alquran.data.Track
import com.likeki.alquran.ui.theme.Gold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    vm: PlaybackViewModel,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onSelectTrack: (Int) -> Unit,
    onDownload: (Track, String) -> Unit,
    downloadingSet: Set<Int>,
    progressMap: Map<Int, Float>,
    baseUrl: String,
    onDelete: (Track) -> Unit,
    onSettings: () -> Unit
) {
    val state by vm.state.collectAsState()
    val track = state.currentTrack

    Box(Modifier.fillMaxSize()) {
        NightSky()

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "AL QURAN",
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 4.sp,
                            color = Color(0xFFF5EFE6)
                        )
                    },
                    actions = {
                        IconButton(onClick = onSettings) {
                            Icon(Icons.Default.MoreVert, "Settings", tint = Color(0xFFF5EFE6))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ProgressRing(
                        progress = if (state.durationMs > 0) state.positionMs.toFloat() / state.durationMs.toFloat() else 0f,
                        size = 240.dp
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "%03d".format(track?.number ?: 0),
                                fontSize = 14.sp,
                                color = Gold,
                                letterSpacing = 3.sp
                            )
                            Spacer(Modifier.height(6.dp))
                            if (!track?.displayNameArabic.isNullOrBlank()) {
                                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                                    Text(
                                        track!!.displayNameArabic,
                                        fontSize = 30.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        textAlign = TextAlign.Center,
                                        color = Color(0xFFF5EFE6)
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                            }
                            Text(
                                track?.displayName ?: "—",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFF5EFE6)
                            )
                            if (state.isPlaying) {
                                Spacer(Modifier.height(6.dp))
                                Text("▶ PLAYING", fontSize = 11.sp, color = Gold, letterSpacing = 2.sp)
                            }
                            Spacer(Modifier.height(6.dp))
                            OwnerCreditInsideRing()
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))
                Slider(
                    value = state.positionMs.toFloat(),
                    onValueChange = { onSeek(it.toLong()) },
                    valueRange = 0f..state.durationMs.coerceAtLeast(1).toFloat(),
                    colors = SliderDefaults.colors(thumbColor = Gold, activeTrackColor = Gold)
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(formatTime(state.positionMs), fontSize = 12.sp, color = Color(0xFFF5EFE6))
                    Text(formatTime(state.durationMs), fontSize = 12.sp, color = Color(0xFFF5EFE6))
                }

                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onPrevious, modifier = Modifier.size(56.dp)) {
                        Icon(Icons.Default.SkipPrevious, "Previous", Modifier.size(36.dp), tint = Color(0xFFF5EFE6))
                    }
                    FilledIconButton(
                        onClick = onPlayPause,
                        modifier = Modifier.size(72.dp),
                        shape = CircleShape
                    ) {
                        Icon(
                            if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            "Play/Pause",
                            Modifier.size(40.dp)
                        )
                    }
                    IconButton(onClick = onNext, modifier = Modifier.size(56.dp)) {
                        Icon(Icons.Default.SkipNext, "Next", Modifier.size(36.dp), tint = Color(0xFFF5EFE6))
                    }
                }

                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    val shuffleColor by animateColorAsState(
                        if (state.shuffle) Gold else Color(0xFFF5EFE6).copy(alpha = 0.5f)
                    )
                    AssistChip(
                        onClick = { vm.toggleShuffle() },
                        label = { Text("Shuffle") },
                        leadingIcon = { Icon(Icons.Default.Shuffle, null, tint = shuffleColor) }
                    )
                    AssistChip(
                        onClick = { vm.cycleRepeat() },
                        label = {
                            Text(when (state.repeatMode) {
                                1 -> "Repeat One"
                                2 -> "Repeat All"
                                else -> "Repeat Off"
                            })
                        },
                        leadingIcon = { Icon(Icons.Default.Repeat, null) }
                    )
                }

                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = state.search,
                    onValueChange = { vm.setSearch(it) },
                    placeholder = { Text("Search by number or name") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(Modifier.height(12.dp))
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    itemsIndexed(state.filteredTracks) { _, t ->
                        TrackRow(
                            track = t,
                            isCurrent = t.number == track?.number,
                            isDownloaded = t.number in state.downloadedSet,
                            isDownloading = t.number in downloadingSet,
                            progress = progressMap[t.number] ?: 0f,
                            onClick = {
                                val idx = state.playlist.tracks.indexOfFirst { it.number == t.number }
                                if (idx >= 0) onSelectTrack(idx)
                            },
                            onDownload = { onDownload(t, baseUrl) },
                            onDelete = { if (t.number in state.downloadedSet) onDelete(t) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressRing(
    progress: Float,
    size: androidx.compose.ui.unit.Dp,
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier.size(size), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxSize(),
            color = Gold,
            strokeWidth = 4.dp,
            trackColor = Color(0xFFF5EFE6).copy(alpha = 0.15f)
        )
        content()
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun TrackRow(
    track: Track,
    isCurrent: Boolean,
    isDownloaded: Boolean,
    isDownloading: Boolean,
    progress: Float,
    onClick: () -> Unit,
    onDownload: () -> Unit,
    onDelete: () -> Unit
) {
    val bg by animateColorAsState(
        if (isCurrent) Gold.copy(alpha = 0.14f) else Color(0xFF0A1435).copy(alpha = 0.45f)
    )
    Surface(
        color = bg,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .combinedClickable(onClick = onClick, onLongClick = onDelete),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(28.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (isCurrent) Gold else Color.Transparent)
            )
            Spacer(Modifier.width(12.dp))
            Text("%03d".format(track.number), fontSize = 13.sp, color = Gold, letterSpacing = 2.sp)
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                if (track.displayNameArabic.isNotBlank()) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        Text(track.displayNameArabic, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFFF5EFE6))
                    }
                }
                Text(track.displayName, fontSize = 13.sp, color = Color(0xFFF5EFE6).copy(alpha = 0.7f))
            }
            when {
                isDownloaded -> Icon(Icons.Default.CheckCircle, "Offline", tint = Gold, modifier = Modifier.size(22.dp))
                isDownloading -> Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = Gold
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("${(progress * 100).toInt()}%", fontSize = 11.sp, color = Color(0xFFF5EFE6))
                }
                else -> IconButton(onClick = onDownload, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.CloudDownload, "Download", tint = Gold, modifier = Modifier.size(22.dp))
                }
            }
        }
    }
}

@Composable
private fun OwnerCreditInsideRing() {
    Text(
        text = "Mohamadu Nurudini Taahir",
        fontSize = 10.sp,
        letterSpacing = 1.sp,
        color = Gold.copy(alpha = 0.7f),
        textAlign = TextAlign.Center
    )
}

private fun formatTime(ms: Long): String {
    val total = (ms / 1000).toInt()
    val m = total / 60
    val s = total % 60
    return "%02d:%02d".format(m, s)
}
