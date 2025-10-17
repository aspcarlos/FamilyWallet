package com.example.familywallet.presentacion.familia

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState

@Composable
fun PantallaConfigFamilia(
    vm: FamiliaViewModel,
    onIrALaFamilia: (String) -> Unit, // navegar a inicio con ese id
    onIrLogin: () -> Unit,            // volver al login
    onCrear: () -> Unit,              // ir a crear familia
    onUnirse: () -> Unit,             // ir a unirse a familia
    onAtras: () -> Unit               // botón “Atrás” abajo a la izquierda
) {
    val scope = rememberCoroutineScope()
    val miFamiliaId by vm.miFamiliaId.collectAsState(initial = null)
    val cargando   by vm.cargando.collectAsState(initial = false)

    var mostrarConfirmacion by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // Refresca al entrar
    LaunchedEffect(Unit) { vm.refrescarMiFamilia() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Configuración Familia",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )

            // Estado: sin familia -> solo Crear / Unirse
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
                    enabled = !cargando,
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text("Ir a mi familia")
                }


                OutlinedButton(
                    onClick = { mostrarConfirmacion = true },
                    enabled = !cargando,
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) { Text("Eliminar familia") }

            }

            // Error (si ocurre al eliminar)
            error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(Modifier.height(8.dp))

            // Botón Atrás (abajo a la izquierda)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                OutlinedButton(onClick = onAtras) { Text("Atrás") }
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
                                    vm.refrescarMiFamilia() // tras borrar, dejará la pantalla solo con Crear/Unirse
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







