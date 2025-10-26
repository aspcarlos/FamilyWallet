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
    ScreenScaffold(
        // Si quisieras un TopAppBar, puedes descomentar:
        // topBar = {
        //     TopAppBar(title = { Text("Movimientos") })
        // }
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
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = "Movimientos de la familia: $familiaId",
                    style = MaterialTheme.typography.headlineMedium
                )

                Button(
                    onClick = onNuevo,
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(50.dp)
                ) {
                    Text("Añadir movimiento")
                }
            }
        }
    }
}




