package com.example.familywallet.theme

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

// DataStore de preferencias para guardar ajustes simples de la app
private val Context.dataStore by preferencesDataStore(name = "settings")

class ThemeRepository(private val context: Context) {

    // Clave booleana para persistir si el tema oscuro está activo
    private val KEY_DARK = booleanPreferencesKey("dark_theme")

    // Flow que emite el estado actual del tema oscuro desde DataStore
    val isDarkFlow = context.dataStore.data.map { it[KEY_DARK] ?: false }

    // Guarda en DataStore el nuevo valor del tema oscuro
    suspend fun setDark(enable: Boolean) {
        context.dataStore.edit { it[KEY_DARK] = enable }
    }
}






