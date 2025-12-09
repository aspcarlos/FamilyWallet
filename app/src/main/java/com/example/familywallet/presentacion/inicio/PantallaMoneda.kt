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
    // Lista de monedas disponibles para el selector.
    val opciones = listOf("EUR","USD","GBP","JPY","MXN")

    // Controla si el menú desplegable está abierto.
    var menuAbierto by remember { mutableStateOf(false) }
    // Guarda la selección del usuario y sobrevive a recomposiciones/config changes.
    var seleccion by rememberSaveable { mutableStateOf(vm.monedaActual) }

    // Scaffold común para mantener estilo y topbar consistentes en la app.
    ScreenScaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    // Botón de volver a la pantalla anterior.
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            // Layout central con padding del scaffold y espaciado interno.
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Título principal de la pantalla.
            Text(
                text = "Cambiar moneda",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )

            // Muestra la moneda que está usando actualmente el ViewModel.
            Text("Moneda actual: ${vm.monedaActual}")

            // Abre el menú para elegir una nueva moneda.
            Button(onClick = { menuAbierto = true }) {
                Text("Seleccionar nueva moneda (${seleccion})")
            }

            // Menú desplegable con las opciones predefinidas.
            DropdownMenu(
                expanded = menuAbierto,
                onDismissRequest = { menuAbierto = false },
                containerColor = MaterialTheme.colorScheme.background
            ) {
                opciones.forEach { code ->
                    DropdownMenuItem(
                        text = { Text(code) },
                        onClick = {
                            // Actualiza la selección local sin aplicar aún el cambio real.
                            seleccion = code
                            menuAbierto = false
                        }
                    )
                }
            }

            // Confirma el cambio: delega la persistencia/aplicación al callback.
            Button(
                onClick = { onGuardar(seleccion) },
                // Solo se habilita si la moneda elegida es distinta a la actual.
                enabled = seleccion != vm.monedaActual
            ) {
                Text("Guardar cambios")
            }
        }
    }
}





