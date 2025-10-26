package com.example.familywallet.presentacion.familia

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCrearFamilia(
    vm: FamiliaViewModel,
    onHecho: (String) -> Unit,
    onAtras: () -> Unit
) {
    var nombreFamilia by remember { mutableStateOf("") }
    var alias by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var cargando by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear familia") },
                navigationIcon = {
                    IconButton(onClick = onAtras) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = nombreFamilia,
                    onValueChange = { nombreFamilia = it; error = null },
                    label = { Text("Nombre familia") },
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

                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }

                Button(
                    enabled = !cargando && nombreFamilia.isNotBlank() && alias.isNotBlank(),
                    onClick = {
                        if (nombreFamilia.isBlank() || alias.isBlank()) {
                            error = "Rellena nombre de familia y alias"
                            return@Button
                        }
                        cargando = true
                        scope.launch {
                            try {
                                val id = vm.crearFamilia(
                                    nombre = nombreFamilia.trim(),
                                    aliasOwner = alias.trim()
                                )
                                error = null
                                onHecho(id)
                            } catch (e: Exception) {
                                error = e.message ?: "Error al crear familia"
                            } finally {
                                cargando = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(0.85f)
                ) {
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
}












