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
    // Scope para lanzar corrutinas desde la UI.
    val scope = rememberCoroutineScope()

    // Estado observado desde el ViewModel.
    val miFamiliaId by vm.miFamiliaId.collectAsState(initial = null)
    val cargando   by vm.cargando.collectAsState(initial = false)

    // Estados locales para diálogos y mensajes de error.
    var mostrarConfirmEliminar by remember { mutableStateOf(false) }
    var mostrarConfirmSalir    by remember { mutableStateOf(false) }
    var error                  by remember { mutableStateOf<String?>(null) }

    // Inicia la escucha en tiempo real del id de familia del usuario.
    LaunchedEffect(Unit) { vm.observarMiFamilia() }

    // Calcula si el usuario actual es admin de la familia mostrada.
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
                // Título principal de la pantalla.
                Text(
                    text = "Configuración Familia",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )

                if (miFamiliaId.isNullOrEmpty()) {
                    // Caso 1: el usuario NO pertenece a ninguna familia.
                    if (cargando) {
                        CircularProgressIndicator()
                    } else {
                        Spacer(Modifier.height(8.dp))

                        // Navega a la pantalla de creación de familia.
                        Button(
                            onClick = onCrear,
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) { Text("Crear familia") }

                        // Navega a la pantalla para enviar solicitud de unión.
                        Button(
                            onClick = onUnirse,
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) { Text("Unirse a familia") }
                    }
                } else {
                    // Caso 2: el usuario SÍ pertenece a una familia.
                    Button(
                        onClick = { miFamiliaId?.let(onIrALaFamilia) },
                        enabled = !cargando,
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) { Text("Ir a mi familia") }

                    if (esAdmin) {
                        // Si es admin, puede eliminar toda la familia.
                        Button(
                            onClick = { mostrarConfirmEliminar = true },
                            enabled = !cargando,
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) { Text("Eliminar familia") }
                    } else {
                        // Si es miembro, puede salir de la familia.
                        Button(
                            onClick = { mostrarConfirmSalir = true },
                            enabled = !cargando,
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) { Text("Salir de la familia") }
                    }
                }

                // Muestra errores de acciones de familia.
                error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Botón de logout global de la app.
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

            // Overlay de carga para bloquear acciones mientras hay operación.
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

            // Diálogo de confirmación para eliminar familia (solo admin).
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

            // Diálogo de confirmación para salir de familia (miembro).
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



















