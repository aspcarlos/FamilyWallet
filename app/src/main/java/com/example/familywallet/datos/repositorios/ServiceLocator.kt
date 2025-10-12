// datos/repositorios/ServiceLocator.kt
package com.example.familywallet.datos.repositorios

import com.google.firebase.firestore.FirebaseFirestore

object ServiceLocator {
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    val movimientosRepo: MovimientoRepositorio by lazy {
        FirebaseMovimientoRepositorio(db = firestore)
    }
    val familiaRepo: FamiliaRepositorio by lazy {
        FirebaseFamiliaRepositorio(db = firestore)
    }

    // 🔹 Usa el repositorio que prefieras
    val authRepo: AuthRepositorio by lazy {
        FirebaseAuthRepositorio()   // o FakeAuthRepositorio()
    }
}





