package com.example.familywallet.presentacion.familia

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun PantallaCrearFamilia(
    vm: FamiliaViewModel,
    onHecho: (String) -> Unit,
    onAtras: () -> Unit
) {
    // Estado local del formulario (nombre, alias, errores y loading).
    var nombreFamilia by remember { mutableStateOf("") }
    var alias by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var cargando by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope() // Scope para lanzar la creación en corrutina.

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Botón de navegación atrás.
        Button(
            onClick = onAtras,          // Vuelve a la pantalla anterior.
            enabled = !cargando,        // Se desactiva mientras se crea la familia.
            modifier = Modifier
                .align(Alignment.BottomStart)
                .height(48.dp)
        ) {
            Text("Atrás")
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Título de la pantalla.
            Text(
                text = "Crear familia",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary
            )

            // Campo para el nombre de la familia.
            OutlinedTextField(
                value = nombreFamilia,
                onValueChange = { nombreFamilia = it; error = null }, // Actualiza texto y limpia error.
                label = { Text("Nombre familia") },
                singleLine = true,
                enabled = !cargando,
                modifier = Modifier.fillMaxWidth(0.85f)
            )

            // Campo para el alias del creador (admin).
            OutlinedTextField(
                value = alias,
                onValueChange = { alias = it; error = null }, // Actualiza texto y limpia error.
                label = { Text("Tu alias") },
                singleLine = true,
                enabled = !cargando,
                modifier = Modifier.fillMaxWidth(0.85f)
            )

            // Muestra un error simple del formulario o del repositorio.
            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            // Botón principal para crear la familia.
            Button(
                enabled = !cargando && nombreFamilia.isNotBlank() && alias.isNotBlank(), // Validación básica.
                onClick = {
                    // Validación rápida antes de llamar al ViewModel.
                    if (nombreFamilia.isBlank() || alias.isBlank()) {
                        error = "Rellena nombre de familia y alias"
                        return@Button
                    }

                    // Activa loading y crea la familia en background.
                    cargando = true
                    scope.launch {
                        try {
                            val id = vm.crearFamilia(
                                nombre = nombreFamilia.trim(),   // Nombre limpio.
                                aliasOwner = alias.trim()        // Alias del admin.
                            )
                            error = null
                            onHecho(id) // Devuelve el id para navegar a Inicio.
                        } catch (e: Exception) {
                            error = e.message ?: "Error al crear familia"
                        } finally {
                            cargando = false // Desactiva loading pase lo que pase.
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(48.dp)
            ) {
                // Indicador de progreso cuando está creando.
                if (cargando) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text("Crear")
                }
            }
        }
    }
}














