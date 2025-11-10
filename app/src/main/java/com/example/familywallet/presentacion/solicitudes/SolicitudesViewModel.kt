package com.example.familywallet.presentacion.solicitudes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.familywallet.datos.modelos.Solicitud
import com.example.familywallet.datos.repositorios.AuthRepositorio
import com.example.familywallet.datos.repositorios.FamiliaRepositorio
import com.example.familywallet.datos.repositorios.SolicitudesRepositorio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SolicitudesViewModel(
    private val solicitudesRepo: SolicitudesRepositorio,
    private val familiaRepo: FamiliaRepositorio,
    private val authRepo: AuthRepositorio
) : ViewModel() {

    private val _pendientes = MutableStateFlow<List<Solicitud>>(emptyList())
    val pendientes: StateFlow<List<Solicitud>> = _pendientes
    private val _procesandoId = MutableStateFlow<String?>(null)
    val procesandoId: StateFlow<String?> = _procesandoId

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun cargarPendientes(familiaId: String) = viewModelScope.launch {
        try {
            _error.value = null
            _pendientes.value = solicitudesRepo.listarPendientes(familiaId)
        } catch (e: Exception) {
            _pendientes.value = emptyList()
            _error.value = e.localizedMessage ?: "No se pudieron cargar las solicitudes"
        }
    }

    fun enviarSolicitudPorNombre(
        nombreFamilia: String,
        aliasSolicitante: String,
        onOk: () -> Unit,
        onError: (String) -> Unit
    ) = viewModelScope.launch {
        try {
            _error.value = null
            val uid = authRepo.usuarioActualUid ?: return@launch onError("No autenticado")
            val familiaId = familiaRepo.buscarFamiliaPorNombre(nombreFamilia)
                ?: return@launch onError("No existe una familia con ese nombre")
            solicitudesRepo.enviarSolicitud(familiaId, uid, aliasSolicitante)
            onOk()
        } catch (e: Exception) {
            val msg = e.localizedMessage ?: "Error enviando solicitud"
            _error.value = msg
            onError(msg)
        }
    }

    fun aceptar(familiaId: String, s: Solicitud) = viewModelScope.launch {
        _procesandoId.value = s.id
        try {
            _error.value = null
            solicitudesRepo.aprobarSolicitud(familiaId, s.uid, s.alias, s.id)
            cargarPendientes(familiaId)
        } catch (e: Exception) {
            _error.value = e.localizedMessage ?: "Error al aceptar"
        } finally {
            _procesandoId.value = null
        }
    }

    fun rechazar(familiaId: String, solicitudId: String) = viewModelScope.launch {
        _procesandoId.value = solicitudId
        try {
            _error.value = null
            solicitudesRepo.rechazar(solicitudId)
            cargarPendientes(familiaId)
        } catch (e: Exception) {
            _error.value = e.localizedMessage ?: "Error al rechazar"
        } finally {
            _procesandoId.value = null
        }
    }
}







