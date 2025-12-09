package com.example.familywallet.datos.repositorios

import com.example.familywallet.datos.modelos.Miembro
import com.example.familywallet.datos.modelos.toRol
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

// Implementación real de FamiliaRepositorio con Firestore.
// Gestiona familias, miembros, pertenencia del usuario y borrado en cascada.
class FirebaseFamiliaRepositorio(
    private val db: FirebaseFirestore
) : FamiliaRepositorio {

    // Referencias a colecciones principales usadas por la app.
    private val familias   get() = db.collection("familias")
    private val miembros   get() = db.collection("miembros")
    private val movimientos get() = db.collection("movimientos")

    // Obtiene el owner/admin de una familia a partir del documento familias/{id}.
    override suspend fun ownerUidDe(familiaId: String): String? = try {
        familias.document(familiaId).get().await().getString("ownerUid")
    } catch (_: Exception) { null }

    // Lista los miembros de una familia leyendo "miembros" y mapeando a Miembro.
    override suspend fun miembrosDe(familiaId: String): List<Miembro> = try {
        miembros.whereEqualTo("familiaId", familiaId)
            .get().await().documents.map { d ->
                Miembro(
                    id    = d.id,
                    uid   = d.getString("uid") ?: "",
                    alias = d.getString("alias") ?: "",
                    rol   = d.getString("rol").toRol()
                )
            }
    } catch (_: Exception) { emptyList() }

    // Observa en tiempo real la familia del usuario.
    // Usa 3 listeners de respaldo: usuarios, familias(owner) y miembros.
    override fun observarMiFamiliaId(uid: String): Flow<String?> = callbackFlow {
        var last: String? = null

        // Evita emitir el mismo id repetido.
        fun emitIfChanged(n: String?) {
            if (n != last) {
                last = n
                trySend(n)
            }
        }

        // Fuente principal: usuarios/{uid}.familiaId
        val regUser = db.collection("usuarios").document(uid)
            .addSnapshotListener { snap, err ->
                if (err != null) return@addSnapshotListener
                val fam = snap?.getString("familiaId")?.takeUnless { it.isBlank() }
                emitIfChanged(fam)
            }

        // Respaldo: si el usuario es owner en familias/.
        val regOwner = db.collection("familias")
            .whereEqualTo("ownerUid", uid)
            .limit(1)
            .addSnapshotListener { s, e ->
                if (e != null) return@addSnapshotListener
                val fam = s?.documents?.firstOrNull()?.id
                emitIfChanged(fam?.takeUnless { it.isBlank() })
            }

        // Respaldo: si el usuario aparece como miembro en miembros/.
        val regMember = db.collection("miembros")
            .whereEqualTo("uid", uid)
            .limit(1)
            .addSnapshotListener { s, e ->
                if (e != null) return@addSnapshotListener
                val fam = s?.documents
                    ?.firstOrNull()
                    ?.getString("familiaId")
                    ?.takeUnless { it.isBlank() }
                emitIfChanged(fam)
            }

        // Libera listeners al cerrar el flow.
        awaitClose {
            regUser.remove()
            regOwner.remove()
            regMember.remove()
        }
    }

    // Expulsa a un miembro: elimina su documento en miembros y borra familiaId en usuarios/{uid}.
    override suspend fun expulsarMiembro(familiaId: String, uidMiembro: String) {
        val miembrosCol = db.collection("miembros")
        val usuario     = db.collection("usuarios").document(uidMiembro)

        val miembroDoc = miembrosCol.whereEqualTo("familiaId", familiaId)
            .whereEqualTo("uid", uidMiembro)
            .limit(1).get().await().documents.firstOrNull()

        db.runBatch { b ->
            miembroDoc?.let { b.delete(it.reference) }
            b.update(usuario, mapOf("familiaId" to FieldValue.delete()))
        }.await()
    }

    // Salir de familia: borra su entrada en miembros y limpia familiaId en usuarios/{uid}.
    override suspend fun salirDeFamilia(familiaId: String, uid: String) {
        val miembrosCol = db.collection("miembros")
        val miembroDoc = miembrosCol.whereEqualTo("familiaId", familiaId)
            .whereEqualTo("uid", uid)
            .limit(1)
            .get()
            .await()
            .documents
            .firstOrNull()

        db.runBatch { b ->
            miembroDoc?.let { b.delete(it.reference) }
            b.update(
                db.collection("usuarios").document(uid),
                mapOf("familiaId" to FieldValue.delete())
            )
        }.await()
    }

    // Devuelve el nombre guardado en familias/{id}.
    override suspend fun nombreDe(familiaId: String): String? = try {
        familias.document(familiaId).get().await().getString("nombre")
    } catch (_: Exception) { null }

    // Búsqueda rápida del id de familia del usuario.
    // Prioriza usuarios/{uid}.familiaId, luego owner, luego miembro.
    override suspend fun miFamiliaId(uid: String): String? {
        val snap = db.collection("usuarios").document(uid).get().await()
        val fam = snap.getString("familiaId")
        if (!fam.isNullOrBlank()) return fam

        val owner = db.collection("familias")
            .whereEqualTo("ownerUid", uid).limit(1).get().await()
            .documents.firstOrNull()?.id
        if (owner != null) return owner

        val miembro = db.collection("miembros")
            .whereEqualTo("uid", uid).limit(1).get().await()
            .documents.firstOrNull()?.getString("familiaId")

        return miembro
    }

    // Crea una familia, registra al owner como admin en miembros
    // y guarda familiaId en usuarios/{ownerUid}.
    override suspend fun crearFamilia(
        nombre: String,
        ownerUid: String,
        aliasOwner: String
    ): String {
        val ref = familias.document()

        ref.set(
            mapOf(
                "nombre"    to nombre,
                "ownerUid"  to ownerUid,
                "createdAt" to System.currentTimeMillis()
            )
        ).await()

        miembros.document().set(
            mapOf(
                "familiaId" to ref.id,
                "uid"       to ownerUid,
                "alias"     to aliasOwner,
                "rol"       to "admin",
                "joinedAt"  to System.currentTimeMillis()
            )
        ).await()

        db.collection("usuarios").document(ownerUid)
            .set(mapOf("familiaId" to ref.id), SetOptions.merge())
            .await()

        return ref.id
    }

    // Elimina una familia limpiando primero referencias en usuarios,
    // luego borrando movimientos y miembros asociados, y finalmente el documento familia.
    override suspend fun eliminarFamilia(familiaId: String) {
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
                batch.update(userRef, mapOf("familiaId" to FieldValue.delete()))
            }
            batch.commit().await()
        }

        deleteByFieldPaged(movimientos, "familiaId", familiaId)
        deleteByFieldPaged(miembros,    "familiaId", familiaId)

        familias.document(familiaId).delete().await()
    }

    // Borrado paginado por campo para evitar límites de batch.
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

    // Busca una familia por su nombre exacto y devuelve su id.
    override suspend fun buscarFamiliaPorNombre(nombre: String): String? {
        val q = familias.whereEqualTo("nombre", nombre).limit(1).get().await()
        return q.documents.firstOrNull()?.id
    }

    // Comprueba si el uid es el owner de la familia.
    override suspend fun esAdmin(familiaId: String, uid: String): Boolean {
        val d = familias.document(familiaId).get().await()
        return d.getString("ownerUid") == uid
    }
}








