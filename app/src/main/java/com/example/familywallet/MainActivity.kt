package com.example.familywallet

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.familywallet.presentacion.autenticacion.PantallaLogin
import com.example.familywallet.presentacion.autenticacion.PantallaRegistro
import com.example.familywallet.presentacion.autenticacion.PantallaOlvidoPassword
import com.example.familywallet.presentacion.familia.PantallaConfigFamilia
import com.example.familywallet.presentacion.familia.PantallaCrearFamilia
import com.example.familywallet.presentacion.familia.PantallaUnirseFamilia
import com.example.familywallet.presentacion.inicio.PantallaInicio
import com.example.familywallet.presentacion.movimientos.*

sealed class Ruta(val route: String) {
    data object Login         : Ruta("login")
    data object Registro      : Ruta("registro")
    data object Recuperar     : Ruta("recuperar")
    data object ConfigFamilia : Ruta("config_familia")
    data object CrearFamilia  : Ruta("crear_familia")
    data object UnirseFamilia : Ruta("unirse_familia")

    data object Inicio        : Ruta("inicio/{familiaId}")
    data object AddGasto      : Ruta("add_gasto/{familiaId}")
    data object AddIngreso    : Ruta("add_ingreso/{familiaId}")

    data object Historial     : Ruta("historial/{familiaId}")
    data object HistorialMes  : Ruta("historial_mes/{familiaId}/{year}/{month}") // month 1..12
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { AppNav() } }
    }
}

@Composable
fun AppNav() {
    val nav = rememberNavController()

    // >>> VM COMPARTIDO <<<
    val movVM: MovimientosViewModel = viewModel()

    NavHost(navController = nav, startDestination = Ruta.Login.route) {

        // LOGIN
        composable(Ruta.Login.route) {
            PantallaLogin(
                onLoginOk = {
                    nav.navigate(Ruta.ConfigFamilia.route) {
                        popUpTo(Ruta.Login.route) { inclusive = true }
                    }
                },
                onRegistro = { nav.navigate(Ruta.Registro.route) },
                onOlvido   = { nav.navigate(Ruta.Recuperar.route) }
            )
        }

        // REGISTRO
        composable(Ruta.Registro.route) {
            PantallaRegistro(
                onRegistrar = { _, _ ->
                    nav.navigate(Ruta.Login.route) {
                        popUpTo(Ruta.Login.route) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onVolverLogin = { nav.popBackStack() }
            )
        }

        // RECUPERAR
        composable(Ruta.Recuperar.route) {
            PantallaOlvidoPassword(
                onEnviado    = { nav.popBackStack() },
                onVolverLogin= { nav.popBackStack() }
            )
        }

        // CONFIG FAMILIA
        composable(Ruta.ConfigFamilia.route) {
            PantallaConfigFamilia(
                onCrear  = { nav.navigate(Ruta.CrearFamilia.route) },
                onUnirse = { nav.navigate(Ruta.UnirseFamilia.route) }
            )
        }

        // CREAR FAMILIA
        composable(Ruta.CrearFamilia.route) {
            PantallaCrearFamilia(
                onHecho = { familiaId ->
                    nav.navigate("inicio/$familiaId") {
                        popUpTo(Ruta.ConfigFamilia.route) { inclusive = true }
                    }
                }
            )
        }

        // UNIRSE FAMILIA
        composable(Ruta.UnirseFamilia.route) {
            PantallaUnirseFamilia(
                onHecho = { familiaId ->
                    nav.navigate("inicio/$familiaId") {
                        popUpTo(Ruta.ConfigFamilia.route) { inclusive = true }
                    }
                }
            )
        }

        // INICIO
        composable(
            route = Ruta.Inicio.route,
            arguments = listOf(navArgument("familiaId"){ type = NavType.StringType })
        ) { backStack ->
            val familiaId = backStack.arguments?.getString("familiaId")
            if (familiaId.isNullOrBlank()) {
                Log.e("FW","familiaId faltante en Inicio")
                nav.popBackStack(); return@composable
            }
            PantallaInicio(
                familiaId = familiaId,
                vm = movVM,
                onIrAddGasto   = { nav.navigate("add_gasto/$familiaId") },
                onIrAddIngreso = { nav.navigate("add_ingreso/$familiaId") },
                onIrHistorial  = { nav.navigate("historial/$familiaId") }
            )
        }

        // ADD GASTO
        composable(
            route = Ruta.AddGasto.route,
            arguments = listOf(navArgument("familiaId"){ type = NavType.StringType })
        ) { backStack ->
            val familiaId = backStack.arguments?.getString("familiaId") ?: return@composable
            PantallaAgregarGasto(
                familiaId = familiaId,
                vm = movVM,
                onGuardado = {
                    nav.navigate("inicio/$familiaId") {
                        popUpTo("inicio/$familiaId") { inclusive = true }
                        launchSingleTop = true; restoreState = true
                    }
                }
            )
        }

        // ADD INGRESO
        composable(
            route = Ruta.AddIngreso.route,
            arguments = listOf(navArgument("familiaId"){ type = NavType.StringType })
        ) { backStack ->
            val familiaId = backStack.arguments?.getString("familiaId") ?: return@composable
            PantallaAgregarIngreso(
                familiaId = familiaId,
                vm = movVM,
                onGuardado = {
                    nav.navigate("inicio/$familiaId") {
                        popUpTo("inicio/$familiaId") { inclusive = true }
                        launchSingleTop = true; restoreState = true
                    }
                }
            )
        }

        // HISTORIAL (lista meses)
        composable(
            route = Ruta.Historial.route,
            arguments = listOf(navArgument("familiaId"){ type = NavType.StringType })
        ) { backStack ->
            val familiaId = backStack.arguments?.getString("familiaId") ?: return@composable
            PantallaHistorial(
                familiaId = familiaId,
                vm = movVM,
                onAbrirMes = { year, month -> nav.navigate("historial_mes/$familiaId/$year/$month") },
                onBack = { nav.popBackStack() }
            )
        }

        // HISTORIAL MES (detalle)
        composable(
            route = Ruta.HistorialMes.route,
            arguments = listOf(
                navArgument("familiaId"){ type = NavType.StringType },
                navArgument("year"){ type = NavType.IntType },
                navArgument("month"){ type = NavType.IntType }, // 1..12
            )
        ) { backStack ->
            val familiaId = backStack.arguments?.getString("familiaId") ?: return@composable
            val year  = backStack.arguments?.getInt("year") ?: return@composable
            val month = backStack.arguments?.getInt("month") ?: return@composable

            PantallaHistorialMes(
                familiaId = familiaId,
                year = year,
                month = month,
                vm = movVM,
                onBack = { nav.popBackStack() }
            )
        }
    }
}





