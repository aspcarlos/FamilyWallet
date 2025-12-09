package com.example.familywallet.presentacion.movimientos

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.familywallet.presentacion.ui.ScreenScaffold

@Composable
fun PantallaMovimientos(
    familiaId: String,
    onNuevo: () -> Unit
) {
    // Scaffold reutilizable de la app para mantener estilo y fondo consistentes
    ScreenScaffold(
        // Aquí no se define topBar ni FAB porque esta pantalla es simple
    ) { padding ->

        // Contenedor principal centrado con padding del scaffold
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {

            // Columna con el título y el botón principal de acción
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {

                // Texto informativo mostrando el id de familia recibido por navegación
                Text(
                    text = "Movimientos de la familia: $familiaId",
                    style = MaterialTheme.typography.headlineMedium
                )

                // Botón que dispara la navegación hacia la pantalla de añadir movimiento
                Button(
                    onClick = onNuevo,
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(50.dp)
                ) {
                    // Texto del botón
                    Text("Añadir movimiento")
                }
            }
        }
    }
}





