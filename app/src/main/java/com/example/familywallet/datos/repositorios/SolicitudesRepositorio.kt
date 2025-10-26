package com.example.familywallet.datos.repositorios

import com.example.familywallet.datos.modelos.Solicitud

interface SolicitudesRepositorio {
    suspend fun enviarSolicitud(familiaId: String, solicitanteUid: String, alias: String)
    suspend fun listarPendientes(familiaId: String): List<Solicitud>
    suspend fun rechazar(solicitudId: String)
    suspend fun aprobarSolicitud(
        familiaId: String,
        uid: String,
        alias: String,
        solicitudId: String
    )
}










