package com.example.familywallet.presentacion.ui

import androidx.compose.runtime.*
import com.example.familywallet.presentacion.familia.FamiliaViewModel

@Composable
fun MembershipGuard(
    familiaIdActual: String,
    familiaVM: FamiliaViewModel,
    onExpulsado: () -> Unit,      // navega a PantallaConfigFamilia
) {
    // Asegura que siempre estamos observando pertenencia
    LaunchedEffect(Unit) { familiaVM.observarMiFamilia() }

    val miFamiliaId by familiaVM.miFamiliaId.collectAsState(initial = null)

    // Si ya no pertenece (o pasa a null), navega fuera
    LaunchedEffect(miFamiliaId) {
        val fuera = miFamiliaId == null || miFamiliaId != familiaIdActual
        if (fuera) onExpulsado()
    }
}


