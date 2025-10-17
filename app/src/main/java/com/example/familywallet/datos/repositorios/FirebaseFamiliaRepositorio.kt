package com.example.familywallet.datos.repositorios

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseFamiliaRepositorio(
    private val db: FirebaseFirestore
) : FamiliaRepositorio {

    private val familias   = db.collection("familias")
    private val miembros   = db.collection("miembros")
    private val movimientos = db.collection("movimientos")

    override suspend fun miFamiliaId(ownerUid: String): String? {
        val q = familias.whereEqualTo("ownerUid", ownerUid).limit(1).get().await()
        return q.documents.firstOrNull()?.id
    }

    override suspend fun crearFamilia(nombre: String, ownerUid: String, aliasOwner: String): String {
        val ref = familias.document()
        ref.set(
            mapOf(
                "nombre" to nombre,
                "ownerUid" to ownerUid,
                "createdAt" to System.currentTimeMillis()
            )
        ).await()

        // miembro ADMIN del owner
        miembros.document().set(
            mapOf(
                "familiaId" to ref.id,
                "uid"       to ownerUid,
                "alias"     to aliasOwner,
                "rol"       to "ADMIN",
                "joinedAt"  to System.currentTimeMillis()
            )
        ).await()

        return ref.id
    }

    override suspend fun eliminarFamilia(familiaId: String) {
        deleteByFieldPaged(movimientos, "familiaId", familiaId)
        deleteByFieldPaged(miembros,    "familiaId", familiaId)
        familias.document(familiaId).delete().await()
    }

    private suspend fun deleteByFieldPaged(
        col: com.google.firebase.firestore.CollectionReference,
        field: String,
        value: String,
        pageSize: Long = 200
    ) {
        while (true) {
            val snap = col.whereEqualTo(field, value).limit(pageSize).get().await()
            if (snap.isEmpty) break
            val batch = db.batch()
            snap.documents.forEach { batch.delete(it.reference) }
            batch.commit().await()
            if (snap.size() < pageSize) break
        }
    }
}





