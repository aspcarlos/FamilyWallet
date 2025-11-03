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
    onIrALaFamilia: (String) -> Unit,
    onCrear: () -> Unit,
    onUnirse: () -> Unit,
    onLogout: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val miFamiliaId by vm.miFamiliaId.collectAsState(initial = null)
    val cargando   by vm.cargando.collectAsState(initial = false)

    var mostrarConfirmacion by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) { vm.observarMiFamilia() }

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
                    if (cargando) {
                        CircularProgressIndicator()
                    } else {
                        Spacer(Modifier.height(8.dp))

                        // VERDE
                        Button(
                            onClick = onCrear,
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) { Text("Crear familia") }

                        // VERDE
                        Button(
                            onClick = onUnirse,
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) { Text("Unirse a familia") }
                    }
                } else {
                    // VERDE
                    Button(
                        onClick = { miFamiliaId?.let(onIrALaFamilia) },
                        enabled = !cargando,
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) { Text("Ir a mi familia") }

                    // VERDE
                    Button(
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

            // VERDE
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
                                        error = null
                                    } catch (e: Exception) {
                                        error = e.message ?: "Error eliminando la familia"
                                    }
                                }
                            }
                        ) { Text("Sí") }
                    },
                    dismissButton = {
                        TextButton(onClick = { mostrarConfirmacion = false }) {
                            Text("No")
                        }
                    }
                )
            }
        }
    }
}














