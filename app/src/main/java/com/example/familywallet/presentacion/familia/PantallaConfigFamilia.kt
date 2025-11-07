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

    // Estados que vienen del ViewModel
    val miFamiliaId by vm.miFamiliaId.collectAsState(initial = null)
    val cargando   by vm.cargando.collectAsState(initial = false)

    var mostrarConfirmacion by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // Empezar a observar la familia solo una vez
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

                Spacer(Modifier.height(8.dp))

                if (miFamiliaId.isNullOrEmpty()) {
                    // Usuario sin familia
                    Button(
                        onClick = onCrear,
                        enabled = !cargando,
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Text("Crear familia")
                    }

                    Button(
                        onClick = onUnirse,
                        enabled = !cargando,
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Text("Unirse a familia")
                    }
                } else {
                    // Usuario ya tiene familia
                    Button(
                        onClick = { miFamiliaId?.let(onIrALaFamilia) },
                        enabled = !cargando,
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Text("Ir a mi familia")
                    }

                    Button(
                        onClick = { mostrarConfirmacion = true },
                        enabled = !cargando,
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Text("Eliminar familia")
                    }
                }

                error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Cerrar sesión siempre visible abajo
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

            // Overlay de carga centrado (no bloquea lógica, solo indica)
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

            // Diálogo de confirmación para eliminar familia
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
                    },
                    containerColor = MaterialTheme.colorScheme.background
                )
            }
        }
    }
}















