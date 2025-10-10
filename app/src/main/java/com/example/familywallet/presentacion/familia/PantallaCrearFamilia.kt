package com.example.familywallet.presentacion.familia

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun PantallaCrearFamilia(
    vm: FamiliaViewModel = viewModel(),
    onHecho: (String) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var cargando by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Crear familia", style = MaterialTheme.typography.headlineSmall)

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre de la familia") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Button(
                onClick = {
                    Log.d("FW", "Click CREAR, nombre = '$nombre'")
                    vm.crearFamilia(
                        nombre = nombre.trim(),
                        onOk = { famId ->
                            Log.d("FW", "Familia creada OK => $famId")
                            error = null
                            onHecho(famId)
                        },
                        onError = { msg ->
                            Log.e("FW", "Error creando familia en UI: $msg")
                            error = msg
                        }
                    )
                }
            ) { Text("Crear") }

        }
    }
}






