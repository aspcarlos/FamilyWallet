package com.example.familywallet.presentacion.familia

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaConfigFamilia(
    vm: FamiliaViewModel,
    onIrALaFamilia: (String) -> Unit, // navegar a inicio con ese id
    onCrear: () -> Unit,              // ir a crear familia
    onUnirse: () -> Unit,             // ir a unirse a familia
    onLogout: () -> Unit              // cierra sesión y navega a login
) {
    val scope = rememberCoroutineScope()

    // Estados del VM
    val miFamiliaId by vm.miFamiliaId.collectAsState(initial = null)
    val cargando   by vm.cargando.collectAsState(initial = false)

    var mostrarConfirmacion by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // IMPORTANTE: observación en tiempo real
    // Así, si en otro dispositivo te aprueban la solicitud,
    // aquí cambia automáticamente a "Ir a mi familia".
    LaunchedEffect(Unit) {
        vm.observarMiFamilia()
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
                    FilledTonalButton(
                        onClick = { miFamiliaId?.let(onIrALaFamilia) },
                        enabled = miFamiliaId != null && !cargando,
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) { Text("Ir a mi familia") }

                    OutlinedButton(
                        onClick = { mostrarConfirmacion = true },
                        enabled = !cargando,
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) { Text("Eliminar familia") }
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
            ) {
                Text("Cerrar sesión")
            }

            // Overlay de carga (bloquea taps) cuando cargando = true
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

            // Diálogo confirmación de borrado
            if (mostrarConfirmacion) {
                AlertDialog(
                    onDismissRequest = { mostrarConfirmacion = false },
                    title = { Text("Eliminar familia") },
                    text = { Text("¿Estás segur@ de que quieres eliminar la familia que has creado?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                mostrarConfirmacion = false
                                scope.launch {
                                    try {
                                        vm.eliminarMiFamiliaCascade()
                                        // No hace falta refrescar a mano; observarMiFamilia() actualizará miFamiliaId
                                        error = null
                                    } catch (e: Exception) {
                                        error = e.message ?: "Error eliminando la familia"
                                    }
                                }
                            }
                        ) { Text("Sí") }
                    },
                    dismissButton = {
                        TextButton(onClick = { mostrarConfirmacion = false }) { Text("No") }
                    }
                )
            }
        }
    }
}












