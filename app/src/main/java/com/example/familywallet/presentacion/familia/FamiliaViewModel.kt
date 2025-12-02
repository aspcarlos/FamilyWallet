package com.example.familywallet.presentacion.familia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.familywallet.datos.repositorios.AuthRepositorio
import com.example.familywallet.datos.repositorios.FamiliaRepositorio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FamiliaViewModel(
    private val familiaRepo: FamiliaRepositorio,
    private val authRepo: AuthRepositorio
) : ViewModel() {

    private var observeJob: Job? = null

    private val _miFamiliaId = MutableStateFlow<String?>(null)
    val miFamiliaId: StateFlow<String?> = _miFamiliaId

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando

    fun observarMiFamilia() {
        val uid = authRepo.usuarioActualUid ?: return

        if (observeJob != null) return

        observeJob = familiaRepo
            .observarMiFamiliaId(uid)
            .onEach { id -> _miFamiliaId.value = id }
            .launchIn(viewModelScope)
    }

    // salir de la familia (para miembros no admin)
    suspend fun salirDeMiFamilia() {
        val uid = authRepo.usuarioActualUid ?: return
        val familiaId = _miFamiliaId.value ?: return

        _cargando.value = true
        try {
            familiaRepo.salirDeFamilia(familiaId, uid)
        } finally {
            _cargando.value = false
        }
    }

    suspend fun refrescarMiFamilia() {
        _cargando.value = true
        try {
            val uid = authRepo.usuarioActualUid ?: return
            _miFamiliaId.value = familiaRepo.miFamiliaId(uid)
        } finally {
            _cargando.value = false
        }
    }

    // Crea la familia y devuelve el id del documento creado.
    suspend fun crearFamilia(nombre: String, aliasOwner: String): String =
        withContext(Dispatchers.IO) {
            val ownerUid = authRepo.usuarioActualUid
                ?: throw IllegalStateException("Usuario no autenticado")

            familiaRepo.crearFamilia(
                nombre = nombre,
                ownerUid = ownerUid,
                aliasOwner = aliasOwner
            )
        }

    fun eliminarMiFamiliaCascade() {
        val id = _miFamiliaId.value ?: return
        viewModelScope.launch {
            _cargando.value = true
            try {
                familiaRepo.eliminarFamilia(id)
                _miFamiliaId.value = null
            } finally {
                _cargando.value = false
            }
        }
    }

    override fun onCleared() {
        observeJob?.cancel()
        super.onCleared()
    }
}
















