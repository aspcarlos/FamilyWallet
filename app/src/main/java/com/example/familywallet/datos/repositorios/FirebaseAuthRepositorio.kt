package com.example.familywallet.datos.repositorios

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepositorio : AuthRepositorio {

    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()

    override val usuarioActualUid: String?
        get() = auth.currentUser?.uid

    override suspend fun login(
        email: String,
        pass: String,
        deviceId: String
    ) {
        val result = auth.signInWithEmailAndPassword(email, pass).await()
        val user = result.user ?: throw Exception("No se pudo iniciar sesión")
        val uid = user.uid

        val docRef = db.collection("userSessions").document(uid)

        db.runTransaction { tx ->
            val snap = tx.get(docRef)
            val active      = snap.getBoolean("active") ?: false
            val otherDevice = snap.getString("deviceId")

            if (active && otherDevice != null && otherDevice != deviceId) {
                throw Exception("ACCOUNT_ALREADY_ACTIVE")
            }

            val data = mapOf(
                "active"      to true,
                "deviceId"    to deviceId,
                "lastUpdated" to FieldValue.serverTimestamp()
            )
            tx.set(docRef, data, SetOptions.merge())
        }.await()
    }

    override suspend fun logout() {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            db.collection("userSessions").document(uid)
                .set(
                    mapOf(
                        "active"      to false,
                        "lastUpdated" to FieldValue.serverTimestamp()
                    ),
                    SetOptions.merge()
                )
                .await()
        }
        auth.signOut()
    }
}



