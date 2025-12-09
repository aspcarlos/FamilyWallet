package com.example.familywallet.presentacion.familia

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.familywallet.presentacion.solicitudes.SolicitudesViewModel

@Composable
fun PantallaUnirseFamilia(
    vm: SolicitudesViewModel,
    onHecho: () -> Unit,
    onAtras: () -> Unit = {}
) {
    // Estados locales del formulario: nombre de familia, alias y control de UI.
    var nombreFamilia by remember { mutableStateOf("") }
    var alias by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var cargando by remember { mutableStateOf(false) }
    var ok by remember { mutableStateOf(false) } // Indica si la solicitud se envió correctamente.

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Título de la pantalla.
            Text(
                text = "Unirse a una familia",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary
            )

            // Campo para escribir el nombre exacto de la familia a buscar.
            OutlinedTextField(
                value = nombreFamilia,
                onValueChange = { nombreFamilia = it }, // Actualiza el nombre introducido.
                label = { Text("Nombre de la familia") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(0.85f)
            )

            // Campo para el alias con el que el usuario quiere aparecer en la familia.
            OutlinedTextField(
                value = alias,
                onValueChange = { alias = it }, // Actualiza el alias introducido.
                label = { Text("Tu alias") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(0.85f)
            )

            // Mensaje de error de validación o del repositorio.
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            // Mensaje informativo si la solicitud se mandó.
            if (ok) Text("Solicitud enviada. Espera la respuesta del administrador.")

            // Botón principal: envía una solicitud de unión usando el ViewModel de solicitudes.
            Button(
                onClick = {
                    // Validación básica de campos obligatorios.
                    if (nombreFamilia.isBlank() || alias.isBlank()) {
                        error = "Rellena nombre de familia y alias"
                        return@Button
                    }

                    // Activa loading y limpia error para iniciar el envío.
                    cargando = true
                    error = null

                    vm.enviarSolicitudPorNombre(
                        nombreFamilia = nombreFamilia.trim(),   // Busca familia por nombre.
                        aliasSolicitante = alias.trim(),        // Alias del solicitante.
                        onOk = {
                            cargando = false
                            ok = true
                            onHecho() // Callback para volver o mostrar siguiente paso.
                        },
                        onError = { msg ->
                            cargando = false
                            error = msg // Muestra el error devuelto por VM/repositorio.
                        }
                    )
                },
                enabled = !cargando, // Evita doble click mientras se envía.
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(50.dp)
            ) {
                Text(if (cargando) "Enviando..." else "Enviar solicitud")
            }
        }

        // Botón de navegación atrás.
        Button(
            onClick = onAtras, // Vuelve a la pantalla anterior.
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 16.dp)
        ) {
            Text("Atrás")
        }
    }
}








