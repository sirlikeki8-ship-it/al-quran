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

    val lastTrack: Flow<Int> = context.dataStore.data.map { it[keyLastTrack] ?: 0 }
    val lastPosition: Flow<Long> = context.dataStore.data.map { it[keyLastPos] ?: 0L }
    val shuffle: Flow<Boolean> = context.dataStore.data.map { it[keyShuffle] ?: false }
    val repeatMode: Flow<Int> = context.dataStore.data.map { it[keyRepeat] ?: 0 }

    suspend fun setLastTrack(n: Int) { context.dataStore.edit { it[keyLastTrack] = n } }
    suspend fun setLastPosition(ms: Long) { context.dataStore.edit { it[keyLastPos] = ms } }
    suspend fun setShuffle(on: Boolean) { context.dataStore.edit { it[keyShuffle] = on } }
    suspend fun setRepeatMode(mode: Int) { context.dataStore.edit { it[keyRepeat] = mode } }
}
