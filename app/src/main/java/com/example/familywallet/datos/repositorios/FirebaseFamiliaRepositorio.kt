package com.example.familywallet.datos.repositorios

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.tasks.await

class FirebaseFamiliaRepositorio(
    private val db: FirebaseFirestore
) : FamiliaRepositorio {

    private val familias  get() = db.collection("familias")
    private val miembros  get() = db.collection("miembros")
    private val movimientos = db.collection("movimientos")


    override suspend fun nombreDe(familiaId: String): String? = try {
        val snap = familias.document(familiaId).get().await()
        // Ajusta el nombre del campo SI en tu documento se llama distinto (p.ej. "nombreFamilia")
        snap.getString("nombre")
    } catch (_: Exception) {
        null
    }

    override suspend fun miFamiliaId(uid: String): String? {
        // Lectura rápida del doc usuarios/{uid}
        val snap = db.collection("usuarios").document(uid).get().await()
        val fam = snap.getString("familiaId")
        if (!fam.isNullOrBlank()) return fam

        // (Opcional) fallback: ¿es owner?
        val owner = db.collection("familias")
            .whereEqualTo("ownerUid", uid).limit(1).get().await()
            .documents.firstOrNull()?.id
        if (owner != null) return owner

        // (Opcional) fallback: ¿miembro?
        val miembro = db.collection("miembros")
            .whereEqualTo("uid", uid).limit(1).get().await()
            .documents.firstOrNull()?.getString("familiaId")

        return miembro
    }

    override fun observarMiFamiliaId(uid: String): Flow<String?> = callbackFlow {
        var last: String? = null
        fun emitIfChanged(n: String?) { if (n != last) { last = n; trySend(n) } }

        // Listener principal: usuarios/{uid}
        val regUser: ListenerRegistration =
            db.collection("usuarios").document(uid)
                .addSnapshotListener { snap, err ->
                    if (err != null) return@addSnapshotListener
                    val fam = snap?.getString("familiaId")
                    emitIfChanged(fam)
                }

        // (Opcional) respaldo: owner
        val regOwner: ListenerRegistration =
            db.collection("familias").whereEqualTo("ownerUid", uid).limit(1)
                .addSnapshotListener { s, e ->
                    if (e != null) return@addSnapshotListener
                    val fam = s?.documents?.firstOrNull()?.id
                    if (!fam.isNullOrBlank()) emitIfChanged(fam)
                }

        // (Opcional) respaldo: miembro
        val regMember: ListenerRegistration =
            db.collection("miembros").whereEqualTo("uid", uid).limit(1)
                .addSnapshotListener { s, e ->
                    if (e != null) return@addSnapshotListener
                    val fam = s?.documents?.firstOrNull()?.getString("familiaId")
                    if (!fam.isNullOrBlank()) emitIfChanged(fam)
                }

        awaitClose {
            regUser.remove()
            regOwner.remove()
            regMember.remove()
        }
    }


    override suspend fun crearFamilia(
        nombre: String,
        ownerUid: String,
        aliasOwner: String
    ): String {
        val ref = familias.document()

        // 1) Documento de la familia
        ref.set(
            mapOf(
                "nombre"   to nombre,
                "ownerUid" to ownerUid,
                "createdAt" to System.currentTimeMillis()
            )
        ).await()

        // 2) Miembro admin
        miembros.document().set(
            mapOf(
                "familiaId" to ref.id,
                "uid"       to ownerUid,
                "alias"     to aliasOwner,
                "rol"       to "admin",
                "joinedAt"  to System.currentTimeMillis()
            )
        ).await()

        // 3) **MUY IMPORTANTE**: reflejar la pertenencia en usuarios/{ownerUid}
        db.collection("usuarios").document(ownerUid)
            .set(mapOf("familiaId" to ref.id), com.google.firebase.firestore.SetOptions.merge())
            .await()

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

    override suspend fun buscarFamiliaPorNombre(nombre: String): String? {
        val q = familias.whereEqualTo("nombre", nombre).limit(1).get().await()
        return q.documents.firstOrNull()?.id
    }

    override suspend fun esAdmin(familiaId: String, uid: String): Boolean {
        val d = familias.document(familiaId).get().await()
        return d.getString("ownerUid") == uid
    }

}





