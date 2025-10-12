package com.example.familywallet

import PantallaCrearFamilia
import android.os.Bundle
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
import com.example.familywallet.datos.repositorios.ServiceLocator
import com.example.familywallet.presentacion.autenticacion.PantallaLogin
import com.example.familywallet.presentacion.autenticacion.PantallaOlvidoPassword
import com.example.familywallet.presentacion.autenticacion.PantallaRegistro
import com.example.familywallet.presentacion.familia.FamiliaVMFactory
import com.example.familywallet.presentacion.familia.FamiliaViewModel
import com.example.familywallet.presentacion.familia.PantallaConfigFamilia
import com.example.familywallet.presentacion.familia.PantallaUnirseFamilia
import com.example.familywallet.presentacion.inicio.PantallaInicio
import com.example.familywallet.presentacion.movimientos.MovimientosVMFactory
import com.example.familywallet.presentacion.movimientos.MovimientosViewModel
import com.example.familywallet.presentacion.movimientos.PantallaAgregarGasto
import com.example.familywallet.presentacion.movimientos.PantallaAgregarIngreso
import com.example.familywallet.presentacion.movimientos.PantallaHistorial
import com.example.familywallet.presentacion.movimientos.PantallaHistorialMes

// ----------------------------
// 📍 Rutas
// ----------------------------
sealed class Ruta(val route: String) {
    data object Login : Ruta("login")
    data object Registro : Ruta("registro")
    data object Recuperar : Ruta("recuperar")
    data object ConfigFamilia : Ruta("config_familia")
    data object CrearFamilia : Ruta("crear_familia")
    data object UnirseFamilia : Ruta("unirse_familia")

    data object Inicio : Ruta("inicio/{familiaId}")

    data object AddGasto : Ruta("add_gasto/{familiaId}")
    data object AddIngreso : Ruta("add_ingreso/{familiaId}")

    data object Historial : Ruta("historial/{familiaId}")
    data object HistorialMes : Ruta("historial_mes/{familiaId}/{anio}/{mes}")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                AppNav()
            }
        }
    }
}

@Composable
fun AppNav() {
    val nav = rememberNavController()

    // VM de movimientos (ya lo tenías parecido)
    val movimientosVM: MovimientosViewModel = viewModel(
        factory = MovimientosVMFactory(ServiceLocator.movimientosRepo)
    )

    // ✅ VM de familia (faltaba)
    val familiaVM: FamiliaViewModel = viewModel(factory = FamiliaVMFactory(
        familiaRepo = ServiceLocator.familiaRepo,
        authRepo = ServiceLocator.authRepo
    ))

    NavHost(
        navController = nav,
        startDestination = Ruta.Login.route
    ) {
        // -------------------------
        // 🔐 Auth
        // -------------------------
        composable(Ruta.Login.route) {
            PantallaLogin(
                onLoginOk = { nav.navigate(Ruta.ConfigFamilia.route) },
                onRegistro = { nav.navigate(Ruta.Registro.route) },
                onOlvido = { nav.navigate(Ruta.Recuperar.route) }
            )
        }

        composable(Ruta.Registro.route) {
            PantallaRegistro(
                onRegistrar = { _, _ ->
                    nav.navigate(Ruta.Login.route)
                },
                onVolverLogin = { nav.popBackStack() }
            )
        }

        composable(Ruta.Recuperar.route) {
            PantallaOlvidoPassword(
                onEnviado = { nav.popBackStack() },
                onVolverLogin = { nav.popBackStack() }
            )
        }

        // -------------------------
        // 👨‍👩‍👧‍👦 Configuración de familia
        // -------------------------
        composable(route = Ruta.ConfigFamilia.route) {
            PantallaConfigFamilia(
                vm = familiaVM,
                onIrALaFamilia = { familiaId ->
                    nav.navigate("inicio/$familiaId") {
                        popUpTo(Ruta.ConfigFamilia.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onIrLogin = {
                    // regresa al login “limpiando” la config de familia del back stack
                    nav.navigate(Ruta.Login.route) {
                        popUpTo(Ruta.ConfigFamilia.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onCrear = { nav.navigate(Ruta.CrearFamilia.route) },
                onUnirse = { nav.navigate(Ruta.UnirseFamilia.route) },
                onAtras = {
                    // “Atrás” desde Config → Login
                    nav.navigate(Ruta.Login.route) {
                        popUpTo(Ruta.ConfigFamilia.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(route = Ruta.CrearFamilia.route) {
            PantallaCrearFamilia(
                vm = familiaVM,
                onHecho = { familiaId ->
                    nav.navigate("inicio/$familiaId") {
                        popUpTo(Ruta.ConfigFamilia.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onAtras = { nav.popBackStack() }
            )
        }

        composable(Ruta.UnirseFamilia.route) {
            PantallaUnirseFamilia(
                onHecho = { familiaId ->
                    nav.navigate("inicio/$familiaId") {
                        popUpTo(Ruta.ConfigFamilia.route) { inclusive = true }
                    }
                }
            )
        }

        // -------------------------
        // 🏠 Inicio (usa VM)
        // -------------------------
        composable(
            route = "inicio/{familiaId}",
            arguments = listOf(navArgument("familiaId") { type = NavType.StringType })
        ) { backStack ->
            val familiaId = backStack.arguments?.getString("familiaId") ?: return@composable

            PantallaInicio(
                familiaId = familiaId,
                vm = movimientosVM,
                onIrAddGasto = { nav.navigate("add_gasto/$familiaId") },
                onIrAddIngreso = { nav.navigate("add_ingreso/$familiaId") },
                onIrHistorial = { nav.navigate("historial/$familiaId") },

                // ✅ Botón "Atrás" que vuelve a ConfigFamilia
                onBackToConfig = {
                    nav.navigate(Ruta.ConfigFamilia.route) {
                        // sacamos esta pantalla del back stack
                        popUpTo("inicio/$familiaId") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // -------------------------
        // ➕ Gasto (usa VM)
        // -------------------------
        composable(
            route = Ruta.AddGasto.route,
            arguments = listOf(navArgument("familiaId") { type = NavType.StringType })
        ) { backStack ->
            val familiaId = backStack.arguments?.getString("familiaId") ?: return@composable

            PantallaAgregarGasto(
                familiaId = familiaId,
                vm = movimientosVM,
                onGuardado = {
                    nav.navigate("inicio/$familiaId") {
                        popUpTo("inicio/$familiaId") { inclusive = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        // -------------------------
        // ➕ Ingreso (usa VM)
        // -------------------------
        composable(
            route = Ruta.AddIngreso.route,
            arguments = listOf(navArgument("familiaId") { type = NavType.StringType })
        ) { backStack ->
            val familiaId = backStack.arguments?.getString("familiaId") ?: return@composable

            PantallaAgregarIngreso(
                familiaId = familiaId,
                vm = movimientosVM,
                onGuardado = {
                    nav.navigate("inicio/$familiaId") {
                        popUpTo("inicio/$familiaId") { inclusive = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        // -------------------------
        // 📚 Historial (lista de meses) — SIN VM
        // -------------------------
        composable(
            route = Ruta.Historial.route,
            arguments = listOf(navArgument("familiaId") { type = NavType.StringType })
        ) { backStack ->
            val familiaId = backStack.arguments?.getString("familiaId") ?: return@composable

            PantallaHistorial(
                familiaId = familiaId,
                onAbrirMes = { year, month ->
                    nav.navigate("historial_mes/$familiaId/$year/$month")
                },
                onBack = { nav.popBackStack() }
            )
        }

        // -------------------------
        // 📅 Historial del mes (detalle) — CON VM
        // -------------------------
        composable(
            route = Ruta.HistorialMes.route,
            arguments = listOf(
                navArgument("familiaId") { type = NavType.StringType },
                navArgument("anio")      { type = NavType.IntType },
                navArgument("mes")       { type = NavType.IntType },
            )
        ) { backStack ->
            val familiaId = backStack.arguments?.getString("familiaId")
            val year      = backStack.arguments?.getInt("anio")
            val month     = backStack.arguments?.getInt("mes")
            if (familiaId == null || year == null || month == null) return@composable

            PantallaHistorialMes(
                familiaId = familiaId,
                year = year,
                month = month,
                vm = movimientosVM,   // ← aquí sí pasa VM
                onBack = { nav.popBackStack() }
            )
        }
    }
}







