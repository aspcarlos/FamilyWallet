package com.example.familywallet.datos.repositorios

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class FirebaseFamiliaRepositorio(
    private val db: FirebaseFirestore
) : FamiliaRepositorio {

    private val familias = db.collection("familias")
    private val miembros = db.collection("miembros")
    private val movimientos = db.collection("movimientos")

    override suspend fun miFamiliaId(ownerUid: String): String? {
        val q = familias.whereEqualTo("ownerUid", ownerUid).limit(1).get().await()
        return q.documents.firstOrNull()?.id
    }

    override suspend fun crearFamilia(nombre: String, ownerUid: String, aliasOwner: String): String {
        val ref = familias.document()
        val data = mapOf(
            "nombre" to nombre,
            "ownerUid" to ownerUid,
            "createdAt" to System.currentTimeMillis()
        )
        ref.set(data).await()

        // (opcional) añadir miembro owner
        miembros.document().set(
            mapOf(
                "familiaID" to ref.id,
                "uidID" to ownerUid,
                "alias" to "admin",
                "rol" to "admin",
                "joinedAt" to System.currentTimeMillis()
            )
        ).await()

        return ref.id
    }

    override suspend fun eliminarFamilia(familiaId: String) {
        // 1) borrar todos los movimientos con familiaID == familiaId
        deleteByFieldPaged(movimientos, "familiaID", familiaId)

        // 2) borrar todos los miembros con familiaID == familiaId
        deleteByFieldPaged(miembros, "familiaID", familiaId)

        // 3) borrar documento familia
        familias.document(familiaId).delete().await()
    }

    private suspend fun deleteByFieldPaged(
        col: com.google.firebase.firestore.CollectionReference,
        field: String,
        value: String,
        pageSize: Long = 200
    ) {
        while (true) {
            val snap = col.whereEqualTo(field, value)
                .limit(pageSize)
                .get().await()
            if (snap.isEmpty) break

            val batch = db.batch()
            snap.documents.forEach { batch.delete(it.reference) }
            batch.commit().await()
            if (snap.size() < pageSize) break
        }
    }
}



