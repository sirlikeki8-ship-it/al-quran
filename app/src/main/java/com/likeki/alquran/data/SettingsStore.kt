package com.likeki.alquran.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("alquran_settings")

class SettingsStore(private val context: Context) {
    private val keyLastTrack = intPreferencesKey("last_track")
    private val keyLastPos = longPreferencesKey("last_pos")
    private val keyShuffle = booleanPreferencesKey("shuffle")
    private val keyRepeat = intPreferencesKey("repeat_mode")

    private val keyAutoPlayNext = booleanPreferencesKey("auto_play_next")
    private val keyResumePos = booleanPreferencesKey("resume_pos")
    private val keyWifiOnly = booleanPreferencesKey("wifi_only")
    private val keyTheme = intPreferencesKey("theme") // 0 system, 1 light, 2 dark
    private val keyTextSize = intPreferencesKey("text_size") // 0 small, 1 normal, 2 large
    private val keyShowArabic = booleanPreferencesKey("show_arabic")

    val lastTrack: Flow<Int> = context.dataStore.data.map { it[keyLastTrack] ?: 0 }
    val lastPosition: Flow<Long> = context.dataStore.data.map { it[keyLastPos] ?: 0L }
    val shuffle: Flow<Boolean> = context.dataStore.data.map { it[keyShuffle] ?: false }
    val repeatMode: Flow<Int> = context.dataStore.data.map { it[keyRepeat] ?: 0 }

    val autoPlayNext: Flow<Boolean> = context.dataStore.data.map { it[keyAutoPlayNext] ?: true }
    val resumePosition: Flow<Boolean> = context.dataStore.data.map { it[keyResumePos] ?: true }
    val wifiOnly: Flow<Boolean> = context.dataStore.data.map { it[keyWifiOnly] ?: false }
    val themeMode: Flow<Int> = context.dataStore.data.map { it[keyTheme] ?: 0 }
    val textSize: Flow<Int> = context.dataStore.data.map { it[keyTextSize] ?: 1 }
    val showArabic: Flow<Boolean> = context.dataStore.data.map { it[keyShowArabic] ?: true }

    suspend fun setLastTrack(n: Int) { context.dataStore.edit { it[keyLastTrack] = n } }
    suspend fun setLastPosition(ms: Long) { context.dataStore.edit { it[keyLastPos] = ms } }
    suspend fun setShuffle(on: Boolean) { context.dataStore.edit { it[keyShuffle] = on } }
    suspend fun setRepeatMode(mode: Int) { context.dataStore.edit { it[keyRepeat] = mode } }
    suspend fun setAutoPlayNext(on: Boolean) { context.dataStore.edit { it[keyAutoPlayNext] = on } }
    suspend fun setResumePosition(on: Boolean) { context.dataStore.edit { it[keyResumePos] = on } }
    suspend fun setWifiOnly(on: Boolean) { context.dataStore.edit { it[keyWifiOnly] = on } }
    suspend fun setThemeMode(mode: Int) { context.dataStore.edit { it[keyTheme] = mode } }
    suspend fun setTextSize(size: Int) { context.dataStore.edit { it[keyTextSize] = size } }
    suspend fun setShowArabic(on: Boolean) { context.dataStore.edit { it[keyShowArabic] = on } }

    suspend fun resetAll() { context.dataStore.edit { it.clear() } }
}
