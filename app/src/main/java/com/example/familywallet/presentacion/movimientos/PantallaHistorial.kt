package com.example.familywallet.presentacion.movimientos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.familywallet.presentacion.familia.FamiliaViewModel
import com.example.familywallet.presentacion.ui.MembershipGuard
import com.example.familywallet.presentacion.ui.ScreenScaffold
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
    MembershipGuard(
        familiaIdActual = familiaId,
        familiaVM = familiaVM,
        onExpulsado = onExpulsado
    )

    val year = Calendar.getInstance().get(Calendar.YEAR)
    val mesesEs = listOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )

    ScreenScaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial $year") },
                navigationIcon = { IconButton(onClick = onBack) { Text("←") } }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(12) { idx ->
                val mesNumero = idx + 1
                val nombreMes = mesesEs[idx]

                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAbrirMes(year, mesNumero) }
                ) {
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









