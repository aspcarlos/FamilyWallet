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

    val miFamiliaId by vm.miFamiliaId.collectAsState(initial = null)
    val cargando   by vm.cargando.collectAsState(initial = false)

    var mostrarConfirmEliminar by remember { mutableStateOf(false) }
    var mostrarConfirmSalir    by remember { mutableStateOf(false) }
    var error                  by remember { mutableStateOf<String?>(null) }

    // arrancar la escucha en tiempo real
    LaunchedEffect(Unit) { vm.observarMiFamilia() }

    // ¿Soy admin de mi familia actual?
    var esAdmin by remember(miFamiliaId) { mutableStateOf(false) }
    LaunchedEffect(miFamiliaId) {
        val famId = miFamiliaId
        if (famId != null) {
            val uid = ServiceLocator.authRepo.usuarioActualUid
            esAdmin = uid != null && ServiceLocator.familiaRepo.esAdmin(famId, uid)
        } else {
            esAdmin = false
        }
    }

    Scaffold { inner ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(24.dp)
        ) {
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
                    // NO está en ninguna familia
                    if (cargando) {
                        CircularProgressIndicator()
                    } else {
                        Spacer(Modifier.height(8.dp))

                        Button(
                            onClick = onCrear,
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) { Text("Crear familia") }

                        Button(
                            onClick = onUnirse,
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) { Text("Unirse a familia") }
                    }
                } else {
                    // SÍ está en una familia
                    Button(
                        onClick = { miFamiliaId?.let(onIrALaFamilia) },
                        enabled = !cargando,
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) { Text("Ir a mi familia") }

                    if (esAdmin) {
                        // CREADOR / ADMIN → puede ELIMINAR la familia
                        Button(
                            onClick = { mostrarConfirmEliminar = true },
                            enabled = !cargando,
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) { Text("Eliminar familia") }
                    } else {
                        // MIEMBRO NORMAL → puede SALIR de la familia
                        Button(
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

            Button(
                onClick = onLogout,
                enabled = !cargando,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(0.6f)
                    .height(48.dp)
            ) {
                Text("Cerrar sesión")
            }

            if (cargando) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.Center),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

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
                        TextButton(onClick = { mostrarConfirmEliminar = false }) {
                            Text("No")
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.background
                )
            }

            if (mostrarConfirmSalir) {
                AlertDialog(
                    onDismissRequest = { mostrarConfirmSalir = false },
                    title = { Text("Salir de la familia") },
                    text = { Text("¿Quieres salir de esta familia? Podrás unirte de nuevo si lo deseas.") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                mostrarConfirmSalir = false
                                scope.launch {
                                    try {
                                        vm.salirDeMiFamilia()
                                        error = null
                                    } catch (e: Exception) {
                                        error = e.message ?: "Error al salir de la familia"
                                    }
                                }
                            }
                        ) { Text("Sí, salir") }
                    },
                    dismissButton = {
                        TextButton(onClick = { mostrarConfirmSalir = false }) {
                            Text("Cancelar")
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.background
                )
            }
        }
    }
}


















