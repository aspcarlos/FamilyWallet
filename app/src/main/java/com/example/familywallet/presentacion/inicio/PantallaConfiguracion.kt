package com.example.familywallet.presentacion.inicio

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.familywallet.presentacion.ui.ScreenScaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaConfiguracion(
    isDark: Boolean,            // Estado actual del tema (viene del ThemeViewModel).
    onToggleDark: () -> Unit,   // Acción para alternar modo claro/oscuro.
    onBack: () -> Unit,         // Navegación hacia atrás.
    onLogout: () -> Unit        // Cierre de sesión desde la configuración.
) {
    // Scaffold común para mantener el estilo consistente en toda la app.
    ScreenScaffold(
        topBar = {
            // Barra superior minimalista con flecha de volver.
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Contenido centrado con dos opciones principales.
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Título de la pantalla.
                Text(
                    text = "Configuración",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                // Opción de tema oscuro.
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Tema oscuro")
                    Switch(
                        checked = isDark,                  // Refleja el estado guardado en DataStore.
                        onCheckedChange = { onToggleDark() } // Llama al VM para persistir el cambio.
                    )
                }

                Divider()

                // Botón para cerrar sesión.
                Button(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cerrar sesión")
                }
            }
        }
    }
}






