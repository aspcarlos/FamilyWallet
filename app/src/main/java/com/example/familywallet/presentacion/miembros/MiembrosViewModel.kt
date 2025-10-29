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

    private val _lista = MutableStateFlow<List<Miembro>>(emptyList())
    val lista: StateFlow<List<Miembro>> = _lista

    private val _ownerUid = MutableStateFlow<String?>(null)
    val ownerUid: StateFlow<String?> = _ownerUid

    private val _procesando = MutableStateFlow<String?>(null) // uid en expulsión
    val procesando: StateFlow<String?> = _procesando

    fun cargar(familiaId: String) = viewModelScope.launch {
        _ownerUid.value = familiaRepo.ownerUidDe(familiaId)
        _lista.value = familiaRepo.miembrosDe(familiaId)
    }

    fun expulsar(familiaId: String, uid: String) = viewModelScope.launch {
        _procesando.value = uid
        try {
            familiaRepo.expulsarMiembro(familiaId, uid)
        } finally {
            _procesando.value = null
            cargar(familiaId) // refresca
        }
    }
}


