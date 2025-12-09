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
    // Observa el estado del VM: lista de miembros, uid del owner y si hay una expulsión en curso.
    val miembros by vm.lista.collectAsState()
    val ownerUid by vm.ownerUid.collectAsState()
    val procesando by vm.procesando.collectAsState()

    // Carga datos cada vez que se entra a una familia distinta.
    LaunchedEffect(familiaId) { vm.cargar(familiaId) }

    Scaffold(
        // Barra superior simple con botón de volver.
        topBar = {
            TopAppBar(
                title = { },
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
                // Layout centrado para título + lista/estado vacío.
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Título de sección.
                Text(
                    text = "Miembros",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                if (miembros.isEmpty()) {
                    // Mensaje cuando no hay miembros cargados/visibles.
                    Text(
                        text = "No hay miembros.",
                        textAlign = TextAlign.Center
                    )
                } else {
                    // Lista de miembros con altura controlada para mantener diseño limpio.
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
                                // Marca al creador/owner con icono.
                                isOwner = (m.uid == ownerUid),
                                // Solo admin puede expulsar, y nunca al owner.
                                mostrarExpulsar = esAdmin && m.uid != ownerUid,
                                // Muestra spinner en el miembro que se está expulsando.
                                expulsando = procesando == m.uid,
                                // Acción de expulsión delegada al VM.
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
    // Fila que muestra alias, icono si es owner y botón de expulsar si corresponde.
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
            // Icono de “admin/owner” para identificar visualmente al creador.
            if (isOwner) {
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = "Admin",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            // Alias del miembro.
            Text(text = item.alias, style = MaterialTheme.typography.titleMedium)
        }

        // Zona de acciones: expulsar (solo admin) y feedback de carga.
        if (mostrarExpulsar) {
            if (expulsando) {
                CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
            } else {
                TextButton(onClick = onExpulsar) { Text("Expulsar") }
            }
        }
    }
}




