package com.example.familywallet.presentacion.miembros

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.familywallet.datos.modelos.Miembro
import com.example.familywallet.datos.repositorios.FamiliaRepositorio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MiembrosViewModel(
    private val familiaRepo: FamiliaRepositorio
) : ViewModel() {

    // Estado reactivo con la lista de miembros de la familia.
    private val _lista = MutableStateFlow<List<Miembro>>(emptyList())
    val lista: StateFlow<List<Miembro>> = _lista

    // UID del owner/admin para poder marcarlo en UI y evitar expulsarlo.
    private val _ownerUid = MutableStateFlow<String?>(null)
    val ownerUid: StateFlow<String?> = _ownerUid

    // UID del miembro que se está expulsando (para mostrar loading por fila).
    private val _procesando = MutableStateFlow<String?>(null)
    val procesando: StateFlow<String?> = _procesando

    // Carga ownerUid y miembros de la familia desde el repositorio.
    fun cargar(familiaId: String) = viewModelScope.launch {
        _ownerUid.value = familiaRepo.ownerUidDe(familiaId)
        _lista.value = familiaRepo.miembrosDe(familiaId)
    }

    // Expulsa un miembro y refresca la lista al terminar.
    fun expulsar(familiaId: String, uid: String) = viewModelScope.launch {
        _procesando.value = uid
        try {
            familiaRepo.expulsarMiembro(familiaId, uid)
        } finally {
            _procesando.value = null
            cargar(familiaId)
        }
    }
}



