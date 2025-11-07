package com.example.familywallet.presentacion.ui

import androidx.compose.runtime.*
import com.example.familywallet.presentacion.familia.FamiliaViewModel

@Composable
fun MembershipGuard(
    familiaIdActual: String,
    familiaVM: FamiliaViewModel,
    onExpulsado: () -> Unit,
) {
    // El ViewModel ya está observando mi familia en el init.
    val miFamiliaId by familiaVM.miFamiliaId.collectAsState(initial = null)

    // Si ya no pertenece (o pasa a null), navega fuera
    LaunchedEffect(key1 = miFamiliaId) {
        val fuera = miFamiliaId == null || miFamiliaId != familiaIdActual
        if (fuera) onExpulsado()
    }
}



