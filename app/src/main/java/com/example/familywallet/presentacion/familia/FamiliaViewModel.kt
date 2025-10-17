package com.example.familywallet.presentacion.familia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.familywallet.datos.repositorios.AuthRepositorio
import com.example.familywallet.datos.repositorios.FamiliaRepositorio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FamiliaViewModel(
    private val familiaRepo: FamiliaRepositorio,
    private val authRepo: AuthRepositorio
) : ViewModel() {

    private val _miFamiliaId = MutableStateFlow<String?>(null)
    val miFamiliaId: StateFlow<String?> = _miFamiliaId

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando

    /**
     * Crea la familia y devuelve el id del documento creado.
     */
    suspend fun crearFamilia(nombre: String, aliasOwner: String): String = withContext(Dispatchers.IO) {
        val ownerUid = authRepo.usuarioActualUid
            ?: throw IllegalStateException("Usuario no autenticado")
        // Llama al repositorio real de Firestore
        familiaRepo.crearFamilia(
            nombre = nombre,
            ownerUid = ownerUid,
            aliasOwner = aliasOwner
        )
    }

    fun refrescarMiFamilia() {
        viewModelScope.launch {
            _cargando.value = true
            try {
                val uid = authRepo.usuarioActualUid
                    ?: run { _miFamiliaId.value = null; return@launch }
                _miFamiliaId.value = familiaRepo.miFamiliaId(uid)
            } finally {
                _cargando.value = false
            }
        }
    }

    fun eliminarMiFamiliaCascade() {
        val id = _miFamiliaId.value ?: return
        viewModelScope.launch {
            _cargando.value = true
            try {
                familiaRepo.eliminarFamilia(id)
                _miFamiliaId.value = null        // forzamos pantalla a “Crear/Unirse”
            } finally {
                _cargando.value = false
            }
        }
    }
}













