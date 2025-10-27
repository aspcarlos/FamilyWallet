package com.example.familywallet.presentacion.familia

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.familywallet.datos.repositorios.ServiceLocator
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaConfigFamilia(
    vm: FamiliaViewModel,
    onIrALaFamilia: (String) -> Unit,
    onCrear: () -> Unit,
    onUnirse: () -> Unit,
    onLogout: () -> Unit
) {
    val scope = rememberCoroutineScope()

    // Estados del VM
    val miFamiliaId by vm.miFamiliaId.collectAsState(initial = null)
    val cargando   by vm.cargando.collectAsState(initial = false)

    // Estado de UI
    var mostrarConfirmEliminar by remember { mutableStateOf(false) }
    var mostrarConfirmSalir by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // ¿Es admin?
    var esAdmin by remember { mutableStateOf(false) }
    val uidActual = remember { ServiceLocator.authRepo.usuarioActualUid }

    // Observación en tiempo real del id de familia (para que cambie de "Crear/Unirse" a "Ir a mi familia")
    LaunchedEffect(Unit) { vm.observarMiFamilia() }

    // Recalcular si es admin cuando cambie la familia o el usuario
    LaunchedEffect(miFamiliaId, uidActual) {
        esAdmin = if (miFamiliaId != null && uidActual != null) {
            runCatching { ServiceLocator.familiaRepo.esAdmin(miFamiliaId!!, uidActual) }
                .getOrDefault(false)
        } else false
    }

    Scaffold { inner ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(24.dp)
        ) {
            // Contenido principal centrado
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Configuración Familia",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )

                if (miFamiliaId.isNullOrEmpty()) {
                    if (cargando) {
                        CircularProgressIndicator()
                    } else {
                        Spacer(Modifier.height(8.dp))

                        FilledTonalButton(
                            onClick = onCrear,
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) { Text("Crear familia") }

                        OutlinedButton(
                            onClick = onUnirse,
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) { Text("Unirse a familia") }
                    }
                } else {
                    // Hay familia
                    FilledTonalButton(
                        onClick = { onIrALaFamilia(miFamiliaId!!) },
                        enabled = !cargando,
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) { Text("Ir a mi familia") }

                    if (esAdmin) {
                        // Dueño → Eliminar la familia
                        OutlinedButton(
                            onClick = { mostrarConfirmEliminar = true },
                            enabled = !cargando,
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) { Text("Eliminar familia") }
                    } else {
                        // Miembro → Salir de la familia
                        OutlinedButton(
                            onClick = { mostrarConfirmSalir = true },
                            enabled = !cargando,
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) { Text("Salir de la familia") }
                    }
                }

                error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Botón Cerrar sesión, fijo abajo y centrado
            OutlinedButton(
                onClick = onLogout,
                enabled = !cargando,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(0.6f)
                    .height(48.dp)
            ) { Text("Cerrar sesión") }

            // Overlay de carga
            if (cargando) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.Center),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }

            // Diálogo confirmación: ELIMINAR (solo admin)
            if (mostrarConfirmEliminar) {
                AlertDialog(
                    onDismissRequest = { mostrarConfirmEliminar = false },
                    title = { Text("Eliminar familia") },
                    text = { Text("¿Estás segur@ de que quieres eliminar la familia que has creado?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                mostrarConfirmEliminar = false
                                scope.launch {
                                    try {
                                        vm.eliminarMiFamiliaCascade()
                                        error = null
                                    } catch (e: Exception) {
                                        error = e.message ?: "Error eliminando la familia"
                                    }
                                }
                            }
                        ) { Text("Sí") }
                    },
                    dismissButton = {
                        TextButton(onClick = { mostrarConfirmEliminar = false }) { Text("No") }
                    }
                )
            }

            // Diálogo confirmación: SALIR (solo miembro)
            if (mostrarConfirmSalir) {
                AlertDialog(
                    onDismissRequest = { mostrarConfirmSalir = false },
                    title = { Text("Salir de la familia") },
                    text = { Text("¿Deseas salir de esta familia? Podrás unirte de nuevo cuando quieras.") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                mostrarConfirmSalir = false
                                scope.launch {
                                    try {
                                        vm.salirDeFamilia()
                                        // El observer actualizará miFamiliaId → null
                                        error = null
                                    } catch (e: Exception) {
                                        error = e.message ?: "No se pudo salir de la familia"
                                    }
                                }
                            }
                        ) { Text("Sí") }
                    },
                    dismissButton = {
                        TextButton(onClick = { mostrarConfirmSalir = false }) { Text("No") }
                    }
                )
            }
        }
    }
}













