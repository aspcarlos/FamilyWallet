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

    // Lista reactiva de solicitudes pendientes para la familia actual
    private val _pendientes = MutableStateFlow<List<Solicitud>>(emptyList())
    val pendientes: StateFlow<List<Solicitud>> = _pendientes

    // Id de la solicitud que se está procesando (para deshabilitar UI y mostrar loader)
    private val _procesandoId = MutableStateFlow<String?>(null)
    val procesandoId: StateFlow<String?> = _procesandoId

    // Estado de error para mostrar mensajes en UI
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Carga desde repositorio todas las solicitudes pendientes de una familia
    fun cargarPendientes(familiaId: String) = viewModelScope.launch {
        try {
            _error.value = null
            _pendientes.value = solicitudesRepo.listarPendientes(familiaId)
        } catch (e: Exception) {
            _pendientes.value = emptyList()
            _error.value = e.localizedMessage ?: "No se pudieron cargar las solicitudes"
        }
    }

    // Envía una solicitud buscando primero la familia por su nombre
    fun enviarSolicitudPorNombre(
        nombreFamilia: String,
        aliasSolicitante: String,
        onOk: () -> Unit,
        onError: (String) -> Unit
    ) = viewModelScope.launch {
        try {
            _error.value = null

            // Obtiene el uid del usuario autenticado
            val uid = authRepo.usuarioActualUid ?: return@launch onError("No autenticado")

            // Traduce el nombre de familia a familiaId
            val familiaId = familiaRepo.buscarFamiliaPorNombre(nombreFamilia)
                ?: return@launch onError("No existe una familia con ese nombre")

            // Crea la solicitud en la capa de datos
            solicitudesRepo.enviarSolicitud(familiaId, uid, aliasSolicitante)

            // Callback de éxito para la pantalla
            onOk()
        } catch (e: Exception) {
            val msg = e.localizedMessage ?: "Error enviando solicitud"
            _error.value = msg
            onError(msg)
        }
    }

    // Aprueba una solicitud: crea miembro, actualiza usuario y elimina la solicitud
    fun aceptar(familiaId: String, s: Solicitud) = viewModelScope.launch {
        _procesandoId.value = s.id
        try {
            _error.value = null

            // Delega en el repositorio la operación atómica/batch
            solicitudesRepo.aprobarSolicitud(familiaId, s.uid, s.alias, s.id)

            // Recarga la lista para reflejar cambios
            cargarPendientes(familiaId)
        } catch (e: Exception) {
            _error.value = e.localizedMessage ?: "Error al aceptar"
        } finally {
            _procesandoId.value = null
        }
    }

    // Rechaza una solicitud eliminándola y refrescando la lista
    fun rechazar(familiaId: String, solicitudId: String) = viewModelScope.launch {
        _procesandoId.value = solicitudId
        try {
            _error.value = null

            // Elimina la solicitud del repositorio
            solicitudesRepo.rechazar(solicitudId)

            // Recarga la lista de pendientes
            cargarPendientes(familiaId)
        } catch (e: Exception) {
            _error.value = e.localizedMessage ?: "Error al rechazar"
        } finally {
            _procesandoId.value = null
        }
    }
}








