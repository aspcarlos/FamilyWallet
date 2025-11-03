package com.example.familywallet.datos.repositorios

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.tasks.await
import kotlin.collections.mapOf
import com.example.familywallet.datos.modelos.Miembro
import com.google.firebase.firestore.SetOptions


class FirebaseFamiliaRepositorio(
    private val db: FirebaseFirestore
) : FamiliaRepositorio {

    private val familias  get() = db.collection("familias")
    private val miembros  get() = db.collection("miembros")
    private val movimientos = db.collection("movimientos")

    override suspend fun ownerUidDe(familiaId: String): String? = try {
        familias.document(familiaId).get().await().getString("ownerUid")
    } catch (_: Exception) { null }

    override suspend fun miembrosDe(familiaId: String): List<Miembro> = try {
        miembros.whereEqualTo("familiaId", familiaId)
            .get().await().documents.map { d ->
                Miembro(
                    id = d.id,
                    uid = d.getString("uid") ?: "",
                    alias = d.getString("alias") ?: "",
                    rol = d.getString("rol") ?: "miembro"
                )
            }
    } catch (_: Exception) { emptyList() }

    override suspend fun expulsarMiembro(familiaId: String, uidMiembro: String) {
        val miembros = db.collection("miembros")
        val usuario  = db.collection("usuarios").document(uidMiembro)

        val miembroDoc = miembros.whereEqualTo("familiaId", familiaId)
            .whereEqualTo("uid", uidMiembro)
            .limit(1).get().await().documents.firstOrNull()

        db.runBatch { b ->
            miembroDoc?.let { b.delete(it.reference) }
            // opción A: eliminar el campo
            b.update(usuario, mapOf("familiaId" to FieldValue.delete()))
            // opción B: setear a null
            // b.set(usuario, mapOf("familiaId" to null), SetOptions.merge())
        }.await()
    }

    override suspend fun salirDeFamilia(uid: String, familiaId: String) {
        // 1) Borra la/s membresía/s del usuario en esa familia
        val snap = miembros
            .whereEqualTo("uid", uid)
            .whereEqualTo("familiaId", familiaId)
            .get()
            .await()

        val batch = db.batch()
        snap.documents.forEach { batch.delete(it.reference) }

        // 2) Limpia el campo familiaId en usuarios/{uid} para que el observer detecte "sin familia"
        val usuarioRef = db.collection("usuarios").document(uid)
        batch.set(usuarioRef, mapOf("familiaId" to FieldValue.delete()), com.google.firebase.firestore.SetOptions.merge())

        batch.commit().await()
    }

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
        fun emitIfChanged(n: String?) {
            if (n != last) {
                last = n
                trySend(n)
            }
        }

        // Listener principal: usuarios/{uid}
        val regUser = db.collection("usuarios").document(uid)
            .addSnapshotListener { snap, err ->
                if (err != null) return@addSnapshotListener
                // si no existe el doc o no tiene familiaId -> null
                val fam = snap?.getString("familiaId")?.takeUnless { it.isBlank() }
                emitIfChanged(fam)
            }

        // Respaldo: es owner de alguna familia
        val regOwner = db.collection("familias")
            .whereEqualTo("ownerUid", uid)
            .limit(1)
            .addSnapshotListener { s, e ->
                if (e != null) return@addSnapshotListener
                val fam = s?.documents?.firstOrNull()?.id
                // si ya no hay familia donde sea owner -> null
                emitIfChanged(fam?.takeUnless { it.isBlank() })
            }

        // Respaldo: es miembro de alguna familia
        val regMember = db.collection("miembros")
            .whereEqualTo("uid", uid)
            .limit(1)
            .addSnapshotListener { s, e ->
                if (e != null) return@addSnapshotListener
                val fam = s?.documents
                    ?.firstOrNull()
                    ?.getString("familiaId")
                    ?.takeUnless { it.isBlank() }
                // si ya no es miembro de ninguna -> null
                emitIfChanged(fam)
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

        // 3) Reflejar la pertenencia en usuarios/{ownerUid}
        db.collection("usuarios").document(ownerUid)
            .set(mapOf("familiaId" to ref.id), SetOptions.merge())
            .await()

        return ref.id
    }


    override suspend fun eliminarFamilia(familiaId: String) {
        // 1) Limpiar familiaId en todos los usuarios miembros de esa familia
        val miembrosSnap = miembros
            .whereEqualTo("familiaId", familiaId)
            .get()
            .await()

        if (!miembrosSnap.isEmpty) {
            val batch = db.batch()
            val usuariosCol = db.collection("usuarios")

            miembrosSnap.documents.forEach { doc ->
                val uid = doc.getString("uid") ?: return@forEach
                val userRef = usuariosCol.document(uid)

                // puedes borrar el campo o ponerlo a null, pero que deje de tener un id válido
                batch.update(userRef, mapOf("familiaId" to FieldValue.delete()))
            }

            batch.commit().await()
        }

        // 2) Borrar movimientos y miembros relacionados (como ya tenías)
        deleteByFieldPaged(movimientos, "familiaId", familiaId)
        deleteByFieldPaged(miembros,    "familiaId", familiaId)

        // 3) Borrar el documento de la familia
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





