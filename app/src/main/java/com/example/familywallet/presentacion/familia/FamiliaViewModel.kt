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

// ViewModel de la funcionalidad "Familia".
// Gestiona el estado de mi familia actual y coordina acciones con los repositorios.
class FamiliaViewModel(
    private val familiaRepo: FamiliaRepositorio,
    private val authRepo: AuthRepositorio
) : ViewModel() {

    // Evita crear varios listeners simultáneos.
    private var observeJob: Job? = null

    // Estado reactivo con el id de la familia del usuario.
    private val _miFamiliaId = MutableStateFlow<String?>(null)
    val miFamiliaId: StateFlow<String?> = _miFamiliaId

    // Estado de carga para mostrar progreso en UI.
    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando

    // Inicia una escucha en tiempo real del id de familia del usuario autenticado.
    fun observarMiFamilia() {
        val uid = authRepo.usuarioActualUid ?: return // si no hay sesión, no observa

        if (observeJob != null) return // evita duplicar el listener

        observeJob = familiaRepo
            .observarMiFamiliaId(uid) // Flow con cambios del id de mi familia
            .onEach { id -> _miFamiliaId.value = id } // actualiza el estado para Compose
            .launchIn(viewModelScope) // se mantiene vivo mientras el VM exista
    }

    // Sale de la familia actual (solo miembros no admin desde la UI).
    suspend fun salirDeMiFamilia() {
        val uid = authRepo.usuarioActualUid ?: return
        val familiaId = _miFamiliaId.value ?: return

        _cargando.value = true
        try {
            familiaRepo.salirDeFamilia(familiaId, uid) // borra pertenencia y limpia usuario
        } finally {
            _cargando.value = false
        }
    }

    // Fuerza una recarga puntual del id de familia sin depender del realtime.
    suspend fun refrescarMiFamilia() {
        _cargando.value = true
        try {
            val uid = authRepo.usuarioActualUid ?: return
            _miFamiliaId.value = familiaRepo.miFamiliaId(uid) // consulta rápida por uid
        } finally {
            _cargando.value = false
        }
    }

    // Crea una familia nueva con el usuario actual como owner/admin.
    // Devuelve el id creado en Firestore.
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

    // Elimina la familia actual del usuario (borrado en cascada desde el repo).
    fun eliminarMiFamiliaCascade() {
        val id = _miFamiliaId.value ?: return
        viewModelScope.launch {
            _cargando.value = true
            try {
                familiaRepo.eliminarFamilia(id) // borra familia, miembros y movimientos relacionados
                _miFamiliaId.value = null // deja la UI en estado "sin familia"
            } finally {
                _cargando.value = false
            }
        }
    }

    // Cancela la escucha en tiempo real cuando el ViewModel se destruye.
    override fun onCleared() {
        observeJob?.cancel()
        super.onCleared()
    }
}

















