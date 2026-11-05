package com.likeki.alquran.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.likeki.alquran.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    vm: SettingsViewModel,
    onBack: () -> Unit,
    onAbout: () -> Unit,
    onDownloadAll: () -> Unit,
    onCancelAll: () -> Unit
) {
    val s by vm.state.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            SectionTitle("Playback")
            ToggleRow("Shuffle", s.shuffle) { vm.toggleShuffle() }
            CycleRow("Repeat", repeatLabel(s.repeatMode)) { vm.cycleRepeat() }
            ToggleRow("Auto-play next track", s.autoPlayNext) { vm.toggleAutoPlayNext() }
            ToggleRow("Resume last position", s.resumePosition) { vm.toggleResumePosition() }

            Spacer(Modifier.height(8.dp))
            SectionTitle("Downloads")
            ToggleRow("Download over Wi-Fi only", s.wifiOnly) { vm.toggleWifiOnly() }
            InfoRow("Storage used", formatBytes(s.storageBytes))
            InfoRow("Downloaded tracks", "${s.downloadedTracks} / ${s.totalTracks}")
            ActionRow("Download all 115 tracks") { onDownloadAll() }
            ActionRow("Cancel in-progress downloads") { onCancelAll() }
            ActionRow("Clear all downloaded audio") { vm.clearDownloads() }

            Spacer(Modifier.height(8.dp))
            SectionTitle("Display")
            CycleRow("Theme", themeLabel(s.themeMode)) { vm.cycleTheme() }
            CycleRow("Text size", textSizeLabel(s.textSize)) { vm.cycleTextSize() }
            ToggleRow("Show Arabic names", s.showArabic) { vm.toggleShowArabic() }

            Spacer(Modifier.height(8.dp))
            SectionTitle("Diagnostics")
            InfoRow("Total tracks configured", s.totalTracks.toString())
            InfoRow("Missing tracks", s.missingTracks.toString())
            InfoRow("Downloaded", s.downloadedTracks.toString())

            Spacer(Modifier.height(8.dp))
            SectionTitle("General")
            ActionRow("About AL QURAN") { onAbout() }
            ActionRow("Reset all settings") { vm.resetSettings() }

            Spacer(Modifier.height(32.dp))
            Text(
                "Mohamadu Nurudini Taahir",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .wrapContentWidth(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text.uppercase(),
        fontSize = 11.sp,
        letterSpacing = 2.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 16.dp, bottom = 6.dp)
    )
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 16.sp)
        Switch(checked = checked, onCheckedChange = { onChange() })
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f))
}

@Composable
private fun CycleRow(label: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 16.sp)
        Text(value, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f))
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 14.sp)
        Text(value, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f))
}

@Composable
private fun ActionRow(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp)
    ) {
        Text(label, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f))
}

private fun repeatLabel(mode: Int) = when (mode) { 1 -> "One"; 2 -> "All"; else -> "Off" }
private fun themeLabel(mode: Int) = when (mode) { 1 -> "Light"; 2 -> "Dark"; else -> "System" }
private fun textSizeLabel(size: Int) = when (size) { 0 -> "Small"; 2 -> "Large"; else -> "Normal" }
private fun formatBytes(b: Long): String = when {
    b < 1024 -> "$b B"
    b < 1024 * 1024 -> "%.1f KB".format(b / 1024.0)
    b < 1024 * 1024 * 1024 -> "%.1f MB".format(b / (1024.0 * 1024.0))
    else -> "%.2f GB".format(b / (1024.0 * 1024.0 * 1024.0))
}
