package com.example.familywallet.presentacion.inicio

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.familywallet.datos.repositorios.ServiceLocator
import com.example.familywallet.presentacion.familia.FamiliaViewModel
import com.example.familywallet.presentacion.movimientos.MovimientosViewModel
import com.example.familywallet.presentacion.ui.MembershipGuard
import com.example.familywallet.presentacion.ui.rangoAnioActual
import com.example.familywallet.presentacion.ui.rangoDiaActual
import com.example.familywallet.presentacion.ui.rangoMesActual
import com.example.familywallet.presentacion.ui.rangoSemanaActual
import com.example.familywallet.presentacion.ui.rememberCurrencyFormatter
import java.util.Locale

@Composable
private fun TriLinesIcon(modifier: Modifier = Modifier) {
    val lineColor = MaterialTheme.colorScheme.onPrimary
    Canvas(modifier = modifier.size(24.dp)) {
        val h = size.height
        val w = size.width
        val stroke = 3f
        drawLine(
            color = lineColor,
            start = Offset(w * 0.25f, h * 0.30f),
            end   = Offset(w * 0.95f, h * 0.30f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = lineColor,
            start = Offset(w * 0.40f, h * 0.50f),
            end   = Offset(w * 0.95f, h * 0.50f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = lineColor,
            start = Offset(w * 0.55f, h * 0.70f),
            end   = Offset(w * 0.95f, h * 0.70f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaInicio(
    familiaId: String,
    vm: MovimientosViewModel,
    familiaVM: FamiliaViewModel,
    onIrAddGasto: () -> Unit,
    onIrAddIngreso: () -> Unit,
    onIrHistorial: () -> Unit,
    onBackToConfig: () -> Unit,
    onAbrirConfiguracion: () -> Unit,
    onCambiarMoneda: () -> Unit = {},
    onVerCategorias: () -> Unit,
    onIrSolicitudes: () -> Unit,
    onVerMiembros: () -> Unit,
    onExpulsado: () -> Unit,
    esAdmin: Boolean = false,
    appName: String = "FamilyWallet"
) {
    MembershipGuard(
        familiaIdActual = familiaId,
        familiaVM = familiaVM,
        onExpulsado = onExpulsado
    )

    LaunchedEffect(familiaId) { vm.cargarMesActual(familiaId) }

    var nombreFamilia by remember(familiaId) { mutableStateOf<String?>(null) }
    LaunchedEffect(familiaId) {
        nombreFamilia = runCatching { ServiceLocator.familiaRepo.nombreDe(familiaId) }.getOrNull()
    }

    val ingresos  = vm.totalIngresos
    val gastos    = vm.totalGastos
    val formatter = rememberCurrencyFormatter(vm.monedaActual)
    val locale    = Locale("es", "ES")

    var menuPeriodoAbierto  by remember { mutableStateOf(false) }
    var menuOpcionesAbierto by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    Text(
                        text = appName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                },
                title = {},
                actions = {
                    Box {
                        IconButton(onClick = { menuPeriodoAbierto = true }) { TriLinesIcon() }
                        DropdownMenu(
                            expanded = menuPeriodoAbierto,
                            onDismissRequest = { menuPeriodoAbierto = false },
                            containerColor = MaterialTheme.colorScheme.background
                        ) {
                            DropdownMenuItem(
                                text = { Text("Día") },
                                onClick = {
                                    vm.aplicarRango(familiaId, rangoDiaActual(locale))
                                    menuPeriodoAbierto = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Semana") },
                                onClick = {
                                    vm.aplicarRango(familiaId, rangoSemanaActual(locale))
                                    menuPeriodoAbierto = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Mes") },
                                onClick = {
                                    vm.aplicarRango(familiaId, rangoMesActual(locale))
                                    menuPeriodoAbierto = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Año") },
                                onClick = {
                                    vm.aplicarRango(familiaId, rangoAnioActual(locale))
                                    menuPeriodoAbierto = false
                                }
                            )
                        }
                    }

                    Spacer(Modifier.width(4.dp))

                    Box {
                        IconButton(onClick = { menuOpcionesAbierto = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Más opciones")
                        }
                        DropdownMenu(
                            expanded = menuOpcionesAbierto,
                            onDismissRequest = { menuOpcionesAbierto = false },
                            containerColor = MaterialTheme.colorScheme.background
                        ) {
                            DropdownMenuItem(
                                text = { Text("Categorías de gastos") },
                                onClick = { menuOpcionesAbierto = false; onVerCategorias() }
                            )
                            DropdownMenuItem(
                                text = { Text("Configuración") },
                                onClick = { menuOpcionesAbierto = false; onAbrirConfiguracion() }
                            )
                            DropdownMenuItem(
                                text = { Text("Moneda") },
                                onClick = { menuOpcionesAbierto = false; onCambiarMoneda() }
                            )

                            if (esAdmin) {
                                Divider()
                                DropdownMenuItem(
                                    text = { Text("Solicitudes") },
                                    onClick = { menuOpcionesAbierto = false; onIrSolicitudes() }
                                )
                            }

                            Divider()
                            DropdownMenuItem(
                                text = { Text("Miembros") },
                                onClick = { menuOpcionesAbierto = false; onVerMiembros() }
                            )
                        }
                    }
                }
            )
        }
    ) { inner ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 24.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!nombreFamilia.isNullOrBlank()) {
                    Text(
                        text = nombreFamilia!!,
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                }

                Text(
                    text = vm.etiquetaPeriodo.ifBlank { vm.nombreMesActual() },
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(Modifier.height(24.dp))

                Text(text = "Ingresos: ${formatter.format(ingresos)}")
                Spacer(Modifier.height(8.dp))
                Text(text = "Gastos: ${formatter.format(gastos)}")
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Resumen: ${formatter.format(ingresos - gastos)}",
                    color = if ((ingresos - gastos) >= 0)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error
                )

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = onIrHistorial,
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(44.dp)
                ) { Text("Ver historial") }

                Spacer(Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onIrAddGasto,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) { Text("Añadir gasto") }

                    Button(
                        onClick = onIrAddIngreso,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) { Text("Añadir ingreso") }
                }
            }

            Button(
                onClick = onBackToConfig,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 16.dp)
                    .height(44.dp)
            ) {
                Text("Atrás")
            }
        }
    }
}






















