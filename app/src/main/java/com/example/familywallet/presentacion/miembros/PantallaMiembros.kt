package com.example.familywallet.presentacion.miembros

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.familywallet.datos.modelos.Miembro

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaMiembros(
    familiaId: String,
    vm: MiembrosViewModel,
    esAdmin: Boolean,
    onBack: () -> Unit
) {
    val miembros by vm.lista.collectAsState()
    val ownerUid by vm.ownerUid.collectAsState()
    val procesando by vm.procesando.collectAsState()

    LaunchedEffect(familiaId) { vm.cargar(familiaId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {  },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { inner ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)              // centra vertical y horizontal
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Título centrado
                Text(
                    text = "Miembros",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                if (miembros.isEmpty()) {
                    // Estado vacío centrado
                    Text(
                        text = "No hay miembros.",
                        textAlign = TextAlign.Center
                    )
                } else {
                    // Lista centrada (con altura limitada para no pegarse al borde)
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(miembros, key = { it.id }) { m ->
                            MiembroRow(
                                item = m,
                                isOwner = (m.uid == ownerUid),
                                mostrarExpulsar = esAdmin && m.uid != ownerUid,
                                expulsando = procesando == m.uid,
                                onExpulsar = { vm.expulsar(familiaId, m.uid) }
                            )
                            Divider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MiembroRow(
    item: Miembro,
    isOwner: Boolean,
    mostrarExpulsar: Boolean,
    expulsando: Boolean,
    onExpulsar: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isOwner) {
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = "Admin",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Text(text = item.alias, style = MaterialTheme.typography.titleMedium)
        }

        if (mostrarExpulsar) {
            if (expulsando) {
                CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
            } else {
                TextButton(onClick = onExpulsar) { Text("Expulsar") }
            }
        }
    }
}



