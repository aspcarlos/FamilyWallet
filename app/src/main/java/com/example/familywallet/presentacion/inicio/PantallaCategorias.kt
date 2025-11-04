package com.example.familywallet.presentacion.inicio

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.familywallet.datos.modelos.Movimiento
import com.example.familywallet.presentacion.movimientos.MovimientosViewModel
import com.example.familywallet.presentacion.ui.ScreenScaffold
import com.example.familywallet.presentacion.ui.rememberCurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCategorias(
    vm: MovimientosViewModel,
    onBack: () -> Unit
) {
    val formatter = rememberCurrencyFormatter(vm.monedaActual)
    val items by vm.itemsDelMesState

    val totalesPorCategoria = remember(items) {
        items
            .asSequence()
            .filter { it.tipo == Movimiento.Tipo.GASTO }
            .groupBy { it.categoria ?: "Otros" }
            .map { (categoria, lista) ->
                categoria to lista.sumOf { it.cantidad }
            }
            .sortedBy { it.first }
    }

    ScreenScaffold(
        topBar = {
            TopAppBar(
                title = { }, // título visual va en el body
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
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Categorías de gasto",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(totalesPorCategoria, key = { it.first }) { (categoria, total) ->
                        ListItem(
                            headlineContent = { Text(categoria) },
                            trailingContent = {
                                Text(
                                    text = formatter.format(total),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        )
                        Divider()
                    }
                }
            }
        }
    }
}







