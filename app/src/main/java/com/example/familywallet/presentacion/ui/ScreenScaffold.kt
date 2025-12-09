package com.example.familywallet.presentacion.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenScaffold(
    topBar: (@Composable () -> Unit)? = null,
    floatingActionButton: (@Composable () -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    // Scaffold base reutilizable para todas las pantallas
    // Centraliza colores de fondo, TopBar y FAB opcionales para mantener consistencia visual

    Scaffold(
        // Color de fondo general de la pantalla
        containerColor = MaterialTheme.colorScheme.background,

        // TopBar opcional (si la pantalla lo necesita)
        topBar = { topBar?.invoke() },

        // FAB opcional (si la pantalla lo necesita)
        floatingActionButton = { floatingActionButton?.invoke() }
    ) { padding ->
        // Surface interna para asegurar fondo uniforme y aplicar los paddings del Scaffold
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            color = MaterialTheme.colorScheme.background
        ) {
            // Contenido real de la pantalla, recibiendo los paddings
            content(padding)
        }
    }
}





