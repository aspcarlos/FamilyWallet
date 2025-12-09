package com.example.familywallet.datos.repositorios

import com.example.familywallet.datos.modelos.Miembro
import kotlinx.coroutines.flow.Flow

// Contrato de gestión de familias y miembros.
// Permite desacoplar la app de Firebase y usar implementaciones fake en pruebas.
interface FamiliaRepositorio {

    // Devuelve el UID del administrador/owner de una familia.
    suspend fun ownerUidDe(familiaId: String): String?

    // Obtiene la lista de miembros asociados a una familia.
    suspend fun miembrosDe(familiaId: String): List<Miembro>

    // Observa en tiempo real el id de la familia del usuario (como owner o miembro).
    fun observarMiFamiliaId(uid: String): Flow<String?>

    // Expulsa a un miembro de la familia y limpia su vínculo.
    suspend fun expulsarMiembro(familiaId: String, uidMiembro: String)

    // Permite que un usuario abandone voluntariamente la familia.
    suspend fun salirDeFamilia(familiaId: String, uid: String)

    // Devuelve el nombre de una familia por su id.
    suspend fun nombreDe(familiaId: String): String?

    // Obtiene rápidamente el id de la familia del usuario (sin listener).
    suspend fun miFamiliaId(uid: String): String?

    // Crea una familia nueva y registra al owner como admin con su alias.
    suspend fun crearFamilia(
        nombre: String,
        ownerUid: String,
        aliasOwner: String
    ): String

    // Elimina una familia y sus datos asociados (operación tipo “cascada”).
    suspend fun eliminarFamilia(familiaId: String)

    // Busca una familia por nombre y devuelve su id si existe.
    suspend fun buscarFamiliaPorNombre(nombre: String): String?

    // Comprueba si un uid tiene rol de administrador en esa familia.
    suspend fun esAdmin(familiaId: String, uid: String): Boolean
}







