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
    var nombreFamilia by remember { mutableStateOf("") }
    var alias by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var cargando by remember { mutableStateOf(false) }
    var ok by remember { mutableStateOf(false) }

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
            Text(
                text = "Unirse a una familia",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = nombreFamilia,
                onValueChange = { nombreFamilia = it },
                label = { Text("Nombre de la familia") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(0.85f)
            )

            OutlinedTextField(
                value = alias,
                onValueChange = { alias = it },
                label = { Text("Tu alias") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(0.85f)
            )

            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            if (ok) Text("Solicitud enviada. Espera la respuesta del administrador.")

            Button(
                onClick = {
                    if (nombreFamilia.isBlank() || alias.isBlank()) {
                        error = "Rellena nombre de familia y alias"
                        return@Button
                    }
                    cargando = true
                    error = null
                    vm.enviarSolicitudPorNombre(
                        nombreFamilia = nombreFamilia.trim(),
                        aliasSolicitante = alias.trim(),
                        onOk = {
                            cargando = false
                            ok = true
                            onHecho()
                        },
                        onError = { msg ->
                            cargando = false
                            error = msg
                        }
                    )
                },
                enabled = !cargando,
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(50.dp)
            ) {
                Text(if (cargando) "Enviando..." else "Enviar solicitud")
            }
        }

        // Botón Atrás abajo a la izquierda, en verde oscuro (usa Button)
        Button(
            onClick = onAtras,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 16.dp)
        ) {
            Text("Atrás")
        }
    }
}







