package com.example.familywallet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.familywallet.datos.repositorios.ServiceLocator
import com.example.familywallet.presentacion.autenticacion.AuthViewModel

import com.example.familywallet.presentacion.autenticacion.PantallaLogin
import com.example.familywallet.presentacion.autenticacion.PantallaOlvidoPassword
import com.example.familywallet.presentacion.autenticacion.PantallaRegistro

import com.example.familywallet.presentacion.familia.FamiliaVMFactory
import com.example.familywallet.presentacion.familia.FamiliaViewModel
import com.example.familywallet.presentacion.familia.PantallaConfigFamilia
import com.example.familywallet.presentacion.familia.PantallaCrearFamilia
import com.example.familywallet.presentacion.familia.PantallaUnirseFamilia

import com.example.familywallet.presentacion.inicio.PantallaCategorias
import com.example.familywallet.presentacion.inicio.PantallaConfiguracion
import com.example.familywallet.presentacion.inicio.PantallaInicio
import com.example.familywallet.presentacion.inicio.PantallaMoneda

import com.example.familywallet.presentacion.movimientos.MovimientosVMFactory
import com.example.familywallet.presentacion.movimientos.MovimientosViewModel
import com.example.familywallet.presentacion.movimientos.PantallaAgregarGasto
import com.example.familywallet.presentacion.movimientos.PantallaAgregarIngreso
import com.example.familywallet.presentacion.movimientos.PantallaHistorial
import com.example.familywallet.presentacion.movimientos.PantallaHistorialMes
import com.example.familywallet.theme.ThemeVMFactory
import com.example.familywallet.theme.ThemeViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

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

    object Configuracion : Ruta("configuracion")
    object Categorias : Ruta("categorias")
    object Moneda : Ruta("moneda")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themeVM: ThemeViewModel = viewModel(factory = ThemeVMFactory(application))
            val isDark by themeVM.isDark.collectAsState()

            MaterialTheme(
                colorScheme = if (isDark) darkColorScheme() else lightColorScheme()
            ) {
                // Esto pinta el fondo de TODA la app con el color del tema
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNav(
                        isDark = isDark,
                        onToggleDark = { themeVM.toggle() }
                    )
                }
            }
        }
    }
}


@Composable
fun AppNav(
    isDark: Boolean,
    onToggleDark: () -> Unit
) {
    val nav = rememberNavController()

    // Si hay usuario, empieza en ConfigFamilia; si no, en Login
    val isLoggedIn = Firebase.auth.currentUser != null
    val start = if (isLoggedIn) Ruta.ConfigFamilia.route else Ruta.Login.route

    // VM de movimientos
    val movimientosVM: MovimientosViewModel = viewModel(
        factory = MovimientosVMFactory(ServiceLocator.movimientosRepo)
    )

    // VM de familia
    val familiaVM: FamiliaViewModel = viewModel(
        factory = FamiliaVMFactory(
            familiaRepo = ServiceLocator.familiaRepo,
            authRepo = ServiceLocator.authRepo
        )
    )

    val authVM: AuthViewModel = viewModel()

    NavHost(
        navController = nav,
        startDestination = Ruta.Login.route
    ) {
        // 🔐 Login
        composable(Ruta.Login.route) {
            // Si ya hay sesión, salta automáticamente a ConfigFamilia
            LaunchedEffect(Unit) {
                Firebase.auth.currentUser?.let {
                    nav.navigate(Ruta.ConfigFamilia.route) {
                        popUpTo(Ruta.Login.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }

            PantallaLogin(
                onLoginOk = {
                    nav.navigate(Ruta.ConfigFamilia.route) {
                        popUpTo(Ruta.Login.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onRegistro = { nav.navigate(Ruta.Registro.route) },
                onOlvido   = { nav.navigate(Ruta.Recuperar.route) }
            )
        }

        composable(Ruta.Registro.route) {
            PantallaRegistro(
                onRegistrar = { _, _ -> nav.navigate(Ruta.Login.route) },
                onVolverLogin = { nav.popBackStack() }
            )
        }

        composable(Ruta.Recuperar.route) {
            PantallaOlvidoPassword(
                onEnviado = { nav.popBackStack() },
                onVolverLogin = { nav.popBackStack() }
            )
        }

        // 👨‍👩‍👧‍👦 Config familia
        composable(Ruta.ConfigFamilia.route) {
            PantallaConfigFamilia(
                vm = familiaVM,
                onIrALaFamilia = { familiaId ->
                    movimientosVM.onFamiliaCambiada(familiaId)
                    nav.navigate("inicio/$familiaId") {
                        popUpTo(Ruta.ConfigFamilia.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onIrLogin = {
                    nav.navigate(Ruta.Login.route) {
                        popUpTo(Ruta.ConfigFamilia.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onCrear = { nav.navigate(Ruta.CrearFamilia.route) },
                onUnirse = { nav.navigate(Ruta.UnirseFamilia.route) },
                onAtras = {
                    nav.navigate(Ruta.Login.route) {
                        popUpTo(Ruta.ConfigFamilia.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Ruta.CrearFamilia.route) {
            PantallaCrearFamilia(
                vm = familiaVM,
                onHecho = { familiaId ->
                    movimientosVM.onFamiliaCambiada(familiaId)   // ← reset + carga
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
                    movimientosVM.onFamiliaCambiada(familiaId)   // ← reset + carga
                    nav.navigate("inicio/$familiaId") {
                        popUpTo(Ruta.ConfigFamilia.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // ⚙️ Configuración (tema oscuro)
        composable(route = Ruta.Configuracion.route) {
            PantallaConfiguracion(
                isDark = isDark,
                onToggleDark = onToggleDark,
                onBack = { nav.popBackStack() },
                onLogout = {
                    authVM.logout()
                    nav.navigate(Ruta.Login.route) {
                        // Limpia todo el back stack para que no se pueda volver
                        popUpTo(nav.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }


        composable(route = Ruta.Moneda.route) {
            PantallaMoneda(
                vm = movimientosVM,
                onGuardar = { nueva ->
                    movimientosVM.cambiarMoneda(nueva)
                    nav.popBackStack()
                },
                onBack = { nav.popBackStack() }
            )
        }

        // 🏷️ Categorías
        composable(Ruta.Categorias.route) {
            PantallaCategorias(
                vm = movimientosVM,
                onBack = { nav.popBackStack() }
            )
        }

        // 🏠 Inicio
        composable(
            route = Ruta.Inicio.route,
            arguments = listOf(navArgument("familiaId"){ type = NavType.StringType })
        ) { backStack ->
            val familiaId = backStack.arguments?.getString("familiaId") ?: return@composable

            PantallaInicio(
                familiaId = familiaId,
                vm = movimientosVM,
                onIrAddGasto = { nav.navigate("add_gasto/$familiaId") },
                onIrAddIngreso = { nav.navigate("add_ingreso/$familiaId") },
                onIrHistorial = { nav.navigate("historial/$familiaId") },
                onBackToConfig = {
                    nav.navigate(Ruta.ConfigFamilia.route) {
                        popUpTo(Ruta.Inicio.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onAbrirConfiguracion = { nav.navigate(Ruta.Configuracion.route) },
                onVerCategorias     = { nav.navigate(Ruta.Categorias.route) },
                onCambiarMoneda     = { nav.navigate(Ruta.Moneda.route) } // <-- AQUÍ
            )
        }


        // ➕ Gasto
        composable(
            route = Ruta.AddGasto.route,
            arguments = listOf(navArgument("familiaId") { type = NavType.StringType })
        ) { backStack ->
            val familiaId = backStack.arguments?.getString("familiaId") ?: return@composable
            PantallaAgregarGasto(
                familiaId = familiaId,
                vm = movimientosVM,
                onGuardado = { nav.popBackStack() } // vuelve a la misma PantallaInicio
            )
        }

        // ➕ Ingreso
        composable(
            route = Ruta.AddIngreso.route,
            arguments = listOf(navArgument("familiaId") { type = NavType.StringType })
        ) { backStack ->
            val familiaId = backStack.arguments?.getString("familiaId") ?: return@composable
            PantallaAgregarIngreso(
                familiaId = familiaId,
                vm = movimientosVM,
                onGuardado = { nav.popBackStack() }
            )
        }

        // 📚 Historial (lista de meses)
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

        // 📅 Historial del mes
        composable(
            route = Ruta.HistorialMes.route,
            arguments = listOf(
                navArgument("familiaId") { type = NavType.StringType },
                navArgument("anio")      { type = NavType.IntType },
                navArgument("mes")       { type = NavType.IntType },
            )
        ) { backStack ->
            val familiaId = backStack.arguments?.getString("familiaId")
            val year = backStack.arguments?.getInt("anio")
            val month = backStack.arguments?.getInt("mes")
            if (familiaId == null || year == null || month == null) return@composable

            PantallaHistorialMes(
                familiaId = familiaId,
                year = year,
                month = month,
                vm = movimientosVM,
                onBack = { nav.popBackStack() }
            )
        }
    }
}








