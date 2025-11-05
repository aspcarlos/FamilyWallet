package com.example.familywallet.presentacion.solicitudes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.familywallet.datos.modelos.Solicitud
import com.example.familywallet.presentacion.familia.FamiliaViewModel
import com.example.familywallet.presentacion.ui.MembershipGuard
import com.example.familywallet.presentacion.ui.ScreenScaffold
import kotlinx.coroutines.launch

// Modelo UI simple
data class SolicitudUi(
    val id: String,
    val alias: String,
    val uid: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaSolicitudes(
    familiaId: String,
    vm: SolicitudesViewModel,
    familiaVM: FamiliaViewModel,
    onBack: () -> Unit,
    onExpulsado: () -> Unit
) {
    MembershipGuard(
        familiaIdActual = familiaId,
        familiaVM = familiaVM,
        onExpulsado = onExpulsado
    )

    val pendientes: List<Solicitud> by vm.pendientes.collectAsState()
    val procesandoId: String? by vm.procesandoId.collectAsState()
    val error: String? by vm.error.collectAsState()

    // Snackbars
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Carga/recarga
    LaunchedEffect(familiaId) { vm.cargarPendientes(familiaId) }

    // Muestra errores del VM
    LaunchedEffect(error) {
        if (!error.isNullOrBlank()) {
            scope.launch { snackbarHostState.showSnackbar(error!!) }
        }
    }

    ScreenScaffold(
        topBar = {
            TopAppBar(
                // Título visual va en el body, aquí lo dejamos vacío
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            if (pendientes.isEmpty()) {
                // Estado vacío: título + mensaje, todo CENTRADO
                val msg = if (error.isNullOrBlank())
                    "No hay solicitudes."
                else
                    "No se pudieron cargar las solicitudes.\nDesliza hacia atrás y vuelve a intentarlo."

                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Solicitudes pendientes",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = msg,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // Lista + título centrado horizontalmente; todo el bloque centrado verticalmente
                val itemsUi = remember(pendientes) {
                    pendientes.map { s -> SolicitudUi(id = s.id, alias = s.alias, uid = s.uid) }
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Solicitudes pendientes",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = itemsUi,
                            key = { it.id }
                        ) { ui ->
                            val disabled = procesandoId == ui.id

                            Box {
                                SolicitudCard(
                                    solicitud = ui,
                                    onAceptar = {
                                        if (!disabled) {
                                            val s = pendientes.firstOrNull { it.id == ui.id }
                                            if (s != null) vm.aceptar(familiaId, s)
                                        }
                                    },
                                    onDenegar = {
                                        if (!disabled) vm.rechazar(familiaId, ui.id)
                                    }
                                )

                                if (disabled) {
                                    Box(
                                        Modifier.matchParentSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Host del snackbar (anclado abajo)
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
            )
        }
    }
}

@Composable
private fun SolicitudCard(
    solicitud: SolicitudUi,
    onAceptar: () -> Unit,
    onDenegar: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "El usuario con el alias \"${solicitud.alias}\" quiere unirse a tu familia.",
                style = MaterialTheme.typography.bodyLarge
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onAceptar,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Aceptar")
                }
                Button(
                    onClick = onDenegar,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Denegar")
                }
            }
        }
    }
}












