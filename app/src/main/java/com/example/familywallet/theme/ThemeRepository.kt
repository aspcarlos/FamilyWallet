package com.example.familywallet.theme

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class ThemeRepository(private val context: Context) {
    private val KEY_DARK = booleanPreferencesKey("dark_theme")
    val isDarkFlow = context.dataStore.data.map { it[KEY_DARK] ?: false }
    suspend fun setDark(enable: Boolean) {
        context.dataStore.edit { it[KEY_DARK] = enable }
    }
}





