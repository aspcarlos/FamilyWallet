package com.example.familywallet.datos.repositorios

import com.google.firebase.firestore.FirebaseFirestore

object ServiceLocator {

    private val firestore by lazy { FirebaseFirestore.getInstance() }

    // Repos
    val movimientosRepo: MovimientoRepositorio by lazy {
        FirebaseMovimientoRepositorio(db = firestore)
    }

    val familiaRepo: FamiliaRepositorio by lazy {
        FirebaseFamiliaRepositorio(db = firestore)
    }

    val authRepo: AuthRepositorio by lazy {
        FirebaseAuthRepositorio()
    }

    val solicitudesRepo: SolicitudesRepositorio by lazy {
        FirebaseSolicitudesRepositorio(db = firestore)
    }
}








