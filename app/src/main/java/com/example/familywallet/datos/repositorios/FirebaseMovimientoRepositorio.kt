package com.example.familywallet.datos.repositorios

import com.example.familywallet.datos.modelos.Movimiento
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseMovimientoRepositorio(private val db: FirebaseFirestore) : MovimientoRepositorio {

    private val movimientosCol = db.collection("movimientos")

    override suspend fun movimientosDeMes(
        familiaId: String,
        year: Int,
        month: Int
    ): List<Movimiento> {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.YEAR, year)
        cal.set(java.util.Calendar.MONTH, month - 1)
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1)
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        val inicio = cal.timeInMillis
        cal.add(java.util.Calendar.MONTH, 1)
        val fin = cal.timeInMillis - 1
        return movimientosEntre(familiaId, inicio, fin)
    }

    override suspend fun movimientosEntre(
        familiaId: String,
        inicioMillis: Long,
        finMillis: Long
    ): List<Movimiento> {
        return try {
            val snap = movimientosCol
                .whereEqualTo("familiaId", familiaId)
                .whereGreaterThanOrEqualTo("fechaMillis", inicioMillis)
                .whereLessThanOrEqualTo("fechaMillis", finMillis)
                .orderBy("fechaMillis", Query.Direction.DESCENDING)
                .get()
                .await()

            snap.documents.mapNotNull { d ->
                val tipo = d.getString("tipo")?.let { runCatching { Movimiento.Tipo.valueOf(it) }.getOrNull() } ?: return@mapNotNull null
                val fecha = d.getLong("fechaMillis") ?: return@mapNotNull null
                Movimiento(
                    id = d.id,
                    familiaId = d.getString("familiaId") ?: return@mapNotNull null,
                    cantidad = d.getDouble("cantidad") ?: 0.0,
                    categoria = d.getString("categoria"),
                    fechaMillis = fecha,
                    tipo = tipo
                )
            }
        } catch (e: com.google.firebase.firestore.FirebaseFirestoreException) {
            if (e.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.FAILED_PRECONDITION) {
                // Índice en construcción → fallback temporal (menos eficiente)
                val snap = movimientosCol
                    .whereEqualTo("familiaId", familiaId)
                    .get()
                    .await()

                snap.documents.mapNotNull { d ->
                    val tipo = d.getString("tipo")?.let { runCatching { Movimiento.Tipo.valueOf(it) }.getOrNull() } ?: return@mapNotNull null
                    val fecha = d.getLong("fechaMillis") ?: return@mapNotNull null
                    Movimiento(
                        id = d.id,
                        familiaId = d.getString("familiaId") ?: return@mapNotNull null,
                        cantidad = d.getDouble("cantidad") ?: 0.0,
                        categoria = d.getString("categoria"),
                        fechaMillis = fecha,
                        tipo = tipo
                    )
                }
                    .filter { it.fechaMillis in inicioMillis..finMillis }
                    .sortedByDescending { it.fechaMillis }
            } else {
                throw e
            }
        }
    }


    override suspend fun agregarMovimiento(m: Movimiento): Movimiento {
        val ref = movimientosCol.add(
            mapOf(
                "familiaId" to m.familiaId,
                "cantidad" to m.cantidad,
                "categoria" to m.categoria,
                "fechaMillis" to m.fechaMillis,
                "tipo" to m.tipo.name
            )
        ).await()
        return m.copy(id = ref.id)
    }

    override suspend fun eliminarMovimiento(familiaId: String, id: String) {
        // familiaId no es necesario para borrar, pero mantenemos la firma de la interfaz
        movimientosCol.document(id).delete().await()
    }

    override fun observarMovimientosFamilia(
        familiaId: String
    ): Flow<List<Movimiento>> = callbackFlow {
        val ref = db.collection("familias")
            .document(familiaId)
            .collection("movimientos")

        val listener = ref.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val lista = snapshot?.toObjects(Movimiento::class.java) ?: emptyList()
            trySend(lista)
        }

        awaitClose { listener.remove() }
    }
}



