package com.example.familywallet.datos.repositorios

import com.example.familywallet.datos.modelos.Movimiento
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Calendar

class FirebaseMovimientoRepositorio(private val db: FirebaseFirestore) : MovimientoRepositorio {

    private val movimientosCol = db.collection("movimientos")

    override suspend fun movimientosDeMes(
        familiaId: String,
        year: Int,
        month: Int
    ): List<Movimiento> {
        // Traemos por familia y filtramos por año/mes en el cliente
        val snap = movimientosCol
            .whereEqualTo("familiaId", familiaId)
            .get()
            .await()

        val cal = Calendar.getInstance()

        return snap.documents.mapNotNull { d ->
            val fecha = d.getLong("fechaMillis") ?: return@mapNotNull null
            val tipoStr = d.getString("tipo") ?: return@mapNotNull null
            val tipo = runCatching { Movimiento.Tipo.valueOf(tipoStr) }.getOrNull() ?: return@mapNotNull null

            cal.timeInMillis = fecha
            val y = cal.get(Calendar.YEAR)
            val m = cal.get(Calendar.MONTH) + 1 // Calendar.MONTH es 0..11

            if (y == year && m == month) {
                Movimiento(
                    id = d.id,
                    familiaId = d.getString("familiaId") ?: familiaId,
                    cantidad = d.getDouble("cantidad") ?: 0.0,
                    categoria = d.getString("categoria"),
                    fechaMillis = fecha,
                    tipo = tipo
                )
            } else null
        }.sortedByDescending { it.fechaMillis }
    }

    override suspend fun agregarMovimiento(m: Movimiento): Movimiento {
        val doc = hashMapOf(
            "familiaId" to m.familiaId,
            "cantidad" to m.cantidad,
            "categoria" to m.categoria,
            "fechaMillis" to m.fechaMillis,   // <- clave correcta en Firestore
            "tipo" to m.tipo.name
        )
        val ref = movimientosCol.add(doc).await()
        return m.copy(id = ref.id)
    }

    // Ajusta la firma a tu interfaz (en tus capturas era eliminarMovimiento(familiaId, id))
    override suspend fun eliminarMovimiento(familiaId: String, id: String) {
        movimientosCol.document(id).delete().await()
    }
}


