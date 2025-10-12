package com.example.familywallet.datos.repositorios

import com.google.firebase.auth.FirebaseAuth

class FirebaseAuthRepositorio : AuthRepositorio {
    private val auth = FirebaseAuth.getInstance()

    override val usuarioActualUid: String?
        get() = auth.currentUser?.uid
}

