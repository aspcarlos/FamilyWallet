package com.example.familywallet.presentacion.ui

import androidx.compose.runtime.*
import com.example.familywallet.presentacion.familia.FamiliaViewModel

// Composable de seguridad: comprueba que el usuario sigue perteneciendo a la familia actual
// Si no pertenece (expulsado o cambió de familia), ejecuta onExpulsado para redirigir
@Composable
fun MembershipGuard(
    familiaIdActual: String,
    familiaVM: FamiliaViewModel,
    onExpulsado: () -> Unit,
) {
    // Observa el id de mi familia desde el ViewModel (estado en tiempo real)
    val miFamiliaId by familiaVM.miFamiliaId.collectAsState(initial = null)

    // Se ejecuta cada vez que cambia miFamiliaId
    LaunchedEffect(key1 = miFamiliaId) {
        // Considera “fuera” si no tengo familia o si no coincide con la familia de la pantalla
        val fuera = miFamiliaId == null || miFamiliaId != familiaIdActual

        // Si estoy fuera, lanza la acción de salida/redirect
        if (fuera) onExpulsado()
    }
}




