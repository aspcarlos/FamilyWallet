package com.example.familywallet.presentacion.inicio

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.familywallet.presentacion.movimientos.MovimientosViewModel
import com.example.familywallet.presentacion.ui.ScreenScaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaMoneda(
    vm: MovimientosViewModel,
    onGuardar: (String) -> Unit,
    onBack: () -> Unit
) {
    val opciones = listOf("EUR","USD","GBP","JPY","MXN")

    var menuAbierto by remember { mutableStateOf(false) }
    var seleccion by rememberSaveable { mutableStateOf(vm.monedaActual) }

    ScreenScaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cambiar Moneda") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Moneda actual: ${vm.monedaActual}")
            Spacer(Modifier.height(24.dp))

            // Selector simple con DropdownMenu
            Button(onClick = { menuAbierto = true }) {
                Text("Seleccionar nueva moneda (${seleccion})")
            }
            DropdownMenu(
                expanded = menuAbierto,
                onDismissRequest = { menuAbierto = false }
            ) {
                opciones.forEach { code ->
                    DropdownMenuItem(
                        text = { Text(code) },
                        onClick = {
                            seleccion = code
                            menuAbierto = false
                        }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { onGuardar(seleccion) },
                enabled = seleccion != vm.monedaActual
            ) {
                Text("Guardar cambios")
            }
        }
    }
}



