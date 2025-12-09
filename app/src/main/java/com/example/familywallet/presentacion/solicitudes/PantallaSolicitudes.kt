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

// Modelo UI simple para no exponer directamente el modelo de datos en la UI
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
    // Guarda de seguridad: si el usuario ya no pertenece a la familia, redirige
    MembershipGuard(
        familiaIdActual = familiaId,
        familiaVM = familiaVM,
        onExpulsado = onExpulsado
    )

    // Estado reactivo de solicitudes pendientes
    val pendientes: List<Solicitud> by vm.pendientes.collectAsState()
    // Id de la solicitud que se está aprobando/rechazando
    val procesandoId: String? by vm.procesandoId.collectAsState()
    // Mensaje de error del ViewModel
    val error: String? by vm.error.collectAsState()

    // Control de snackbars
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Carga inicial de solicitudes al entrar o cambiar de familia
    LaunchedEffect(familiaId) { vm.cargarPendientes(familiaId) }

    // Muestra errores del ViewModel en un snackbar
    LaunchedEffect(error) {
        if (!error.isNullOrBlank()) {
            scope.launch { snackbarHostState.showSnackbar(error!!) }
        }
    }

    // Scaffold común para mantener estilo consistente con el resto de pantallas
    ScreenScaffold(
        topBar = {
            // AppBar simple con botón de vuelta
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->

        // Contenedor principal de la pantalla
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            // Estado vacío: no hay solicitudes o hubo error al cargar
            if (pendientes.isEmpty()) {
                val msg = if (error.isNullOrBlank())
                    "No hay solicitudes."
                else
                    "No se pudieron cargar las solicitudes.\nDesliza hacia atrás y vuelve a intentarlo."

                // Mensaje centrado con título
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
                // Transformación a modelo UI para simplificar la lista
                val itemsUi = remember(pendientes) {
                    pendientes.map { s -> SolicitudUi(id = s.id, alias = s.alias, uid = s.uid) }
                }

                // Columna centrada con título y lista
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Título de la pantalla
                    Text(
                        text = "Solicitudes pendientes",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Lista de solicitudes
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = itemsUi,
                            key = { it.id }
                        ) { ui ->
                            // Deshabilita interacción si esa solicitud se está procesando
                            val disabled = procesandoId == ui.id

                            // Box para superponer loader sobre la tarjeta
                            Box {
                                // Tarjeta con acciones de aceptar/denegar
                                SolicitudCard(
                                    solicitud = ui,
                                    onAceptar = {
                                        // Busca el objeto completo antes de aprobar
                                        if (!disabled) {
                                            val s = pendientes.firstOrNull { it.id == ui.id }
                                            if (s != null) vm.aceptar(familiaId, s)
                                        }
                                    },
                                    onDenegar = {
                                        // Rechaza por id
                                        if (!disabled) vm.rechazar(familiaId, ui.id)
                                    }
                                )

                                // Indicador de carga encima de la tarjeta mientras se procesa
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

            // Host del snackbar anclado abajo
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
    // Card elevada para resaltar cada solicitud
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        // Contenido principal de la tarjeta
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Mensaje de la solicitud
            Text(
                text = "El usuario con el alias \"${solicitud.alias}\" quiere unirse a tu familia.",
                style = MaterialTheme.typography.bodyLarge
            )

            // Botones de acción
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Acepta la solicitud
                Button(
                    onClick = onAceptar,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Aceptar")
                }
                // Deniega la solicitud
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













