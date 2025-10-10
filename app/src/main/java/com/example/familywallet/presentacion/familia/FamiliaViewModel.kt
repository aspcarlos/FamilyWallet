package com.example.familywallet.presentacion.familia

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.familywallet.datos.modelos.Familia
import com.example.familywallet.datos.repositorios.FakeAuthRepositorio
import com.example.familywallet.datos.repositorios.FakeFamiliaRepositorio
import kotlinx.coroutines.launch

class FamiliaViewModel : ViewModel() {
    var familiaActual: Familia? = null
        private set

    fun crearFamilia(
        nombre: String,
        onOk: (String) -> Unit,
        onError: (String) -> Unit
    ) = viewModelScope.launch {
        try {
            Log.d("FW", "UID antes = ${FakeAuthRepositorio.usuarioActualId()}")

            var uid = FakeAuthRepositorio.usuarioActualId()
            if (uid == null) {
                FakeAuthRepositorio.setUsuario("demo-uid")
                uid = FakeAuthRepositorio.usuarioActualId()
                Log.d("FW", "UID asignado automáticamente = $uid")
            }
            if (uid == null) {
                onError("No hay usuario autenticado")
                return@launch
            }

            val fam = FakeFamiliaRepositorio.crearFamilia(uid, nombre)
            familiaActual = fam
            Log.d("FW", "Familia creada correctamente: ${fam.id}")
            onOk(fam.id)

        } catch (e: Exception) {
            Log.e("FW", "Error creando familia: ${e.message}")
            onError(e.message ?: "Error creando familia")
        }
    }
}






