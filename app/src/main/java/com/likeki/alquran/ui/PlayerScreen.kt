package com.likeki.alquran.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.likeki.alquran.PlaybackViewModel
import com.likeki.alquran.ui.theme.Gold
import com.likeki.alquran.data.Track

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    vm: PlaybackViewModel,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onSelectTrack: (Int) -> Unit,
    onDownload: (com.likeki.alquran.data.Track, String) -> Unit,
    downloadingSet: Set<Int>,
    progressMap: Map<Int, Float>,
    baseUrl: String,
    onDelete: (com.likeki.alquran.data.Track) -> Unit,
    onSettings: () -> Unit
) {
    val state by vm.state.collectAsState()
    val track = state.currentTrack

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "AL QURAN",
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 4.sp
                    )
                },
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            // -- Circular player ring --
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                ProgressRing(
                    progress = if (state.durationMs > 0)
                        state.positionMs.toFloat() / state.durationMs.toFloat() else 0f,
                    size = 240.dp
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "%03d".format(track?.number ?: 0),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 3.sp
                        )
                        Spacer(Modifier.height(6.dp))
                        if (!track?.displayNameArabic.isNullOrBlank()) {
                            CompositionLocalProvider(
                                LocalLayoutDirection provides LayoutDirection.Rtl
                            ) {
                                Text(
                                    text = track!!.displayNameArabic,
                                    fontSize = 30.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                        }
                        Text(
                            text = track?.displayName ?: "—",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        if (state.isPlaying) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "▶ PLAYING",
                                fontSize = 11.sp,
                                color = Gold,
                                letterSpacing = 2.sp
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        OwnerCreditInsideRing()
                    }
                }
            }

            // -- Progress bar --
            Spacer(Modifier.height(20.dp))
            Slider(
                value = state.positionMs.toFloat(),
                onValueChange = { onSeek(it.toLong()) },
                valueRange = 0f..state.durationMs.coerceAtLeast(1).toFloat(),
                colors = SliderDefaults.colors(
                    thumbColor = Gold,
                    activeTrackColor = Gold
                )
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(formatTime(state.positionMs), fontSize = 12.sp)
                Text(formatTime(state.durationMs), fontSize = 12.sp)
            }

            // -- Transport controls --
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPrevious, modifier = Modifier.size(56.dp)) {
                    Icon(Icons.Default.SkipPrevious, "Previous", Modifier.size(36.dp))
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
                    Icon(Icons.Default.SkipNext, "Next", Modifier.size(36.dp))
                }
            }

            // -- Toggles --
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val shuffleColor by animateColorAsState(
                    if (state.shuffle) Gold else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
                AssistChip(
                    onClick = { vm.toggleShuffle() },
                    label = { Text("Shuffle") },
                    leadingIcon = {
                        Icon(Icons.Default.Shuffle, null, tint = shuffleColor)
                    }
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
                    leadingIcon = {
                        Icon(Icons.Default.Repeat, null)
                    }
                )
            }

            // -- Search --
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

            // -- Track list --
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
            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
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
        if (isCurrent) MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
        else MaterialTheme.colorScheme.background
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
                    .background(if (isCurrent) Gold else MaterialTheme.colorScheme.background)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                "%03d".format(track.number),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                if (track.displayNameArabic.isNotBlank()) {
                    CompositionLocalProvider(
                        LocalLayoutDirection provides LayoutDirection.Rtl
                    ) {
                        Text(
                            track.displayNameArabic,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Text(
                    track.displayName,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
            when {
                isDownloaded -> Icon(
                    Icons.Default.CheckCircle, "Offline",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                isDownloading -> Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = Gold
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("${(progress * 100).toInt()}%", fontSize = 11.sp)
                }
                else -> IconButton(onClick = onDownload, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.CloudDownload, "Download",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val total = (ms / 1000).toInt()
    val m = total / 60
    val s = total % 60
    return "%02d:%02d".format(m, s)
}


@Composable
private fun OwnerCreditInsideRing() {
    Text(
        text = "Mohamadu Nurudini Taahir",
        fontSize = 10.sp,
        letterSpacing = 1.sp,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
        textAlign = TextAlign.Center
    )
}
