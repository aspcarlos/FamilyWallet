package com.example.familywallet.presentacion.familia

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.familywallet.presentacion.solicitudes.SolicitudesViewModel
import com.example.familywallet.presentacion.ui.ScreenScaffold

@OptIn(ExperimentalMaterial3Api::class)
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

    ScreenScaffold(
        topBar = {
            TopAppBar(
                title = { Text("Unirse a una familia") },
                navigationIcon = {
                    TextButton(onClick = onAtras) { Text("Atrás") }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {

                OutlinedTextField(
                    value = nombreFamilia,
                    onValueChange = { nombreFamilia = it; error = null },
                    label = { Text("Nombre de la familia") },
                    singleLine = true,
                    enabled = !cargando,
                    modifier = Modifier.fillMaxWidth(0.85f)
                )

                OutlinedTextField(
                    value = alias,
                    onValueChange = { alias = it; error = null },
                    label = { Text("Tu alias") },
                    singleLine = true,
                    enabled = !cargando,
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
                    if (cargando) CircularProgressIndicator(strokeWidth = 2.dp)
                    else Text("Enviar solicitud")
                }
            }
        }
    }
}






