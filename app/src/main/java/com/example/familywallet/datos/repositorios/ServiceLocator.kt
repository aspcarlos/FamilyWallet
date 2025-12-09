package com.example.familywallet.datos.repositorios

import com.google.firebase.firestore.FirebaseFirestore

// Localizador de servicios simple.
// Centraliza la creación de repositorios y la instancia de Firestore.
object ServiceLocator {

    // Instancia única de Firestore reutilizada en toda la app.
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    // Repositorio de movimientos (ingresos/gastos) usando Firebase.
    val movimientosRepo: MovimientoRepositorio by lazy {
        FirebaseMovimientoRepositorio(db = firestore)
    }

    // Repositorio de familias y miembros usando Firebase.
    val familiaRepo: FamiliaRepositorio by lazy {
        FirebaseFamiliaRepositorio(db = firestore)
    }

    // Repositorio de autenticación con control de sesión por dispositivo.
    val authRepo: AuthRepositorio by lazy {
        FirebaseAuthRepositorio()
    }

    // Repositorio de solicitudes para unirse a familia.
    val solicitudesRepo: SolicitudesRepositorio by lazy {
        FirebaseSolicitudesRepositorio(db = firestore)
    }
}









