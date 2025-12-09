package com.example.familywallet.datos.repositorios

import com.example.familywallet.datos.modelos.Solicitud

// Contrato para gestionar solicitudes de unión a una familia.
// Permite usar Firebase o repos fake sin cambiar ViewModels ni UI.
interface SolicitudesRepositorio {

    // Crea una solicitud para unirse a una familia con el alias del solicitante.
    suspend fun enviarSolicitud(
        familiaId: String,
        solicitanteUid: String,
        alias: String
    )

    // Lista las solicitudes pendientes de una familia.
    suspend fun listarPendientes(
        familiaId: String
    ): List<Solicitud>

    // Rechaza una solicitud eliminándola.
    suspend fun rechazar(
        solicitudId: String
    )

    // Aprueba una solicitud:
    // añade el usuario como miembro y actualiza su familiaId.
    suspend fun aprobarSolicitud(
        familiaId: String,
        uid: String,
        alias: String,
        solicitudId: String
    )
}











