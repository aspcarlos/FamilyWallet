package com.example.familywallet.datos.repositorios

import android.util.Log
import com.example.familywallet.datos.modelos.Solicitud
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

// Implementación real de SolicitudesRepositorio con Firestore.
// Gestiona el flujo de petición para unirse a una familia (enviar, listar, aprobar, rechazar).
class FirebaseSolicitudesRepositorio(
    private val db: FirebaseFirestore
) : SolicitudesRepositorio {

    // Crea una solicitud en la colección "solicitudes" con estado pendiente.
    override suspend fun enviarSolicitud(
        familiaId: String,
        solicitanteUid: String,
        alias: String
    ) {
        db.collection("solicitudes").add(
            mapOf(
                "familiaId" to familiaId,
                "uid"       to solicitanteUid,
                "alias"     to alias,
                "estado"    to "pendiente",
                "createdAt" to System.currentTimeMillis()
            )
        ).await()
    }

    // Devuelve las solicitudes pendientes de una familia ordenadas por fecha más reciente.
    // Mapea cada documento a un objeto Solicitud.
    override suspend fun listarPendientes(familiaId: String): List<Solicitud> = try {
        db.collection("solicitudes")
            .whereEqualTo("familiaId", familiaId)
            .whereEqualTo("estado", "pendiente")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()
            .documents
            .map { d ->
                val uid   = d.getString("uid") ?: d.getString("solicitanteUid").orElse("")
                val alias = d.getString("alias").orElse("")
                Solicitud(
                    id   = d.id,
                    uid  = uid,
                    alias= alias
                )
            }
    } catch (_: Exception) {
        emptyList()
    }

    // Helper de compatibilidad para campos que puedan venir nulos en Firestore.
    private fun String?.orElse(def: String) = this ?: def

    // Rechaza una solicitud eliminando su documento.
    override suspend fun rechazar(solicitudId: String) {
        db.collection("solicitudes").document(solicitudId).delete().await()
    }

    // Aprueba una solicitud:
    // 1) crea miembro en "miembros"
    // 2) asigna familiaId en "usuarios/{uid}"
    // 3) elimina la solicitud
    // Todo en un batch para mantener consistencia.
    override suspend fun aprobarSolicitud(
        familiaId: String,
        uid: String,
        alias: String,
        solicitudId: String
    ) {
        require(uid.isNotBlank()) { "uid vacío al aprobarSolicitud" }

        val miembrosCol    = db.collection("miembros")
        val solicitudesCol = db.collection("solicitudes")
        val usuarioRef     = db.collection("usuarios").document(uid)

        Log.d("FW", "aprobarSolicitud() -> usuarioRef.path = ${usuarioRef.path}")

        try {
            db.runBatch { b ->
                b.set(
                    miembrosCol.document(),
                    mapOf(
                        "familiaId" to familiaId,
                        "uid"       to uid,
                        "alias"     to alias,
                        "rol"       to "miembro",
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
                b.set(
                    usuarioRef,
                    mapOf("familiaId" to familiaId),
                    SetOptions.merge()
                )
                b.delete(solicitudesCol.document(solicitudId))
            }.await()
        } catch (e: Exception) {
            Log.e("FW", "Fallo en aprobarSolicitud", e)
            throw e
        }
    }
}







