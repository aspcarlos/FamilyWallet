package com.example.familywallet.datos.repositorios

import com.example.familywallet.datos.modelos.Movimiento
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Calendar

class FirebaseMovimientoRepositorio(
    private val db: FirebaseFirestore
) : MovimientoRepositorio {

    private val movimientosCol = db.collection("movimientos")

    // --------- helpers ---------

    private fun docToMovimiento(d: DocumentSnapshot): Movimiento? {
        val tipoStr = d.getString("tipo") ?: return null
        val tipo = runCatching { Movimiento.Tipo.valueOf(tipoStr) }.getOrNull() ?: return null
        val fecha = d.getLong("fechaMillis") ?: return null
        val familiaId = d.getString("familiaId") ?: return null

        return Movimiento(
            id          = d.id,
            familiaId   = familiaId,
            cantidad    = d.getDouble("cantidad") ?: 0.0,
            categoria   = d.getString("categoria"),
            nota        = d.getString("nota"),     // ⬅ leemos la nota
            fechaMillis = fecha,
            tipo        = tipo
        )
    }

    // --------- consultas ---------

    override suspend fun movimientosDeMes(
        familiaId: String,
        year: Int,
        month: Int
    ): List<Movimiento> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month - 1)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        val inicio = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
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

            snap.documents.mapNotNull { docToMovimiento(it) }
        } catch (e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.FAILED_PRECONDITION) {
                // índice aún sin crear -> fallback sin orderBy
                val snap = movimientosCol
                    .whereEqualTo("familiaId", familiaId)
                    .get()
                    .await()

                snap.documents
                    .mapNotNull { docToMovimiento(it) }
                    .filter { it.fechaMillis in inicioMillis..finMillis }
                    .sortedByDescending { it.fechaMillis }
            } else {
                throw e
            }
        }
    }

    // --------- escritura ---------

    override suspend fun agregarMovimiento(m: Movimiento): Movimiento {
        val data = mapOf(
            "familiaId"   to m.familiaId,
            "cantidad"    to m.cantidad,
            "categoria"   to m.categoria,
            "nota"        to m.nota,               // ⬅ guardamos la nota
            "fechaMillis" to m.fechaMillis,
            "tipo"        to m.tipo.name           // "GASTO" / "INGRESO"
        )

        val ref = movimientosCol.add(data).await()
        return m.copy(id = ref.id)
    }

    override suspend fun eliminarMovimiento(familiaId: String, id: String) {
        // familiaId no hace falta, se mantiene por compatibilidad
        movimientosCol.document(id).delete().await()
    }

    // --------- tiempo real ---------

    override fun observarMovimientosFamilia(
        familiaId: String
    ): Flow<List<Movimiento>> = callbackFlow {
        // IMPORTANTE: misma colección que usamos para guardar ("movimientos")
        val listener = movimientosCol
            .whereEqualTo("familiaId", familiaId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val lista = snapshot
                    ?.documents
                    ?.mapNotNull { docToMovimiento(it) }
                    ?: emptyList()

                trySend(lista)
            }

        awaitClose { listener.remove() }
    }
}




