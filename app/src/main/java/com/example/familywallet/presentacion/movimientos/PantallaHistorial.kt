package com.example.familywallet.presentacion.movimientos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.familywallet.presentacion.familia.FamiliaViewModel
import com.example.familywallet.presentacion.ui.MembershipGuard
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaHistorial(
    familiaId: String,
    familiaVM: FamiliaViewModel,
    onAbrirMes: (Int, Int) -> Unit,
    onBack: () -> Unit,
    onExpulsado: () -> Unit
) {
    // Protege la pantalla: si el usuario ya no pertenece a la familia actual, redirige
    MembershipGuard(
        familiaIdActual = familiaId,
        familiaVM = familiaVM,
        onExpulsado = onExpulsado
    )

    // Obtiene el año actual para mostrar el historial del año en curso
    val year = Calendar.getInstance().get(Calendar.YEAR)

    // Lista de nombres de meses en español para pintar las tarjetas
    val mesesEs = listOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )

    // Estructura base con TopAppBar y contenido
    Scaffold(
        topBar = {
            TopAppBar(
                title = {  },
                navigationIcon = {
                    // Botón de vuelta a la pantalla anterior
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->

        // Contenedor principal de la pantalla
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Columna centrada que contiene título y lista de meses
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(16.dp))

                // Título del historial con el año actual
                Text(
                    text = "Historial $year",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(24.dp))

                // Lista de 12 tarjetas, una por cada mes
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Genera 12 elementos correspondientes a los meses del año
                    items(12) { idx ->
                        val mesNumero = idx + 1
                        val nombreMes = mesesEs[idx]

                        // Tarjeta clicable que navega al detalle del mes seleccionado
                        ElevatedCard(
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAbrirMes(year, mesNumero) }
                        ) {
                            // Fila con el nombre del mes y un texto de acción
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(nombreMes, style = MaterialTheme.typography.titleMedium)
                                Text("Ver detalles", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }
        }
    }
}












