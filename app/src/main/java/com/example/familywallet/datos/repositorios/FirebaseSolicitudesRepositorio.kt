package com.example.familywallet.datos.repositorios

import android.util.Log
import com.example.familywallet.datos.modelos.Solicitud
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirebaseSolicitudesRepositorio(
    private val db: FirebaseFirestore
) : SolicitudesRepositorio {

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

    private fun String?.orElse(def: String) = this ?: def

    override suspend fun rechazar(solicitudId: String) {
        db.collection("solicitudes").document(solicitudId).delete().await()
    }

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
            Log.e("FW", "Fallo en aprobarSolicitud", e)   // ⬅️ verás el stack exacto en Logcat
            throw e
        }
    }
}






