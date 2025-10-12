package com.example.familywallet.datos.repositorios

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Date

class FirebaseFamiliaRepositorio(private val db: FirebaseFirestore) : FamiliaRepositorio {

    override suspend fun crearFamilia(
        nombre: String,
        ownerUid: String,
        aliasOwner: String
    ): String {
        try {
            Log.d("FW", "Creando familia: $nombre por $ownerUid")

            // 1️⃣ Crear la familia en la colección 'familias'
            val familia = mapOf(
                "nombre" to nombre,
                "ownerUid" to ownerUid,
                "createdAt" to Date()
            )

            val famRef = db.collection("familias").add(familia).await()
            val familiaId = famRef.id

            Log.d("FW", "Familia creada con id=$familiaId")

            // 2️⃣ Crear el miembro administrador (owner) en 'miembros'
            val miembro = mapOf(
                "uidID" to ownerUid,
                "alias" to aliasOwner,
                "familiaID" to familiaId,
                "rol" to "admin",
                "joinedAt" to Date()
            )

            db.collection("miembros").add(miembro).await()

            Log.d("FW", "Miembro owner insertado correctamente")

            return familiaId
        } catch (e: Exception) {
            Log.e("FW", "❌ Error creando familia: ${e.message}", e)
            throw e
        }
    }
}

