package com.example.familywallet.datos.repositorios

object FakeAuthRepositorio {
    private var uid: String? = null

    fun setUsuario(id: String) { uid = id }
    fun usuarioActualId(): String? = uid
}



