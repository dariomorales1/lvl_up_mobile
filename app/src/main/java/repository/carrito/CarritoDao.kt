package cl.duoc.level_up_mobile.repository.carrito

import androidx.room.*
import cl.duoc.level_up_mobile.model.CarritoItem
import kotlinx.coroutines.flow.Flow

@Dao
interface CarritoDao {

    // ✅ ACTUALIZADO: Filtrar por userId
    @Query("SELECT * FROM carrito_items WHERE userId = :userId ORDER BY agregadoEn DESC")
    fun obtenerPorUsuario(userId: String): Flow<List<CarritoItem>>

    // ✅ ACTUALIZADO: Filtrar por userId y código
    @Query("SELECT * FROM carrito_items WHERE userId = :userId AND productoCodigo = :codigo")
    suspend fun obtenerPorCodigo(userId: String, codigo: String): CarritoItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(item: CarritoItem)

    @Update
    suspend fun actualizar(item: CarritoItem)

    @Delete
    suspend fun eliminar(item: CarritoItem)

    // ✅ ACTUALIZADO: Filtrar por userId y código
    @Query("DELETE FROM carrito_items WHERE userId = :userId AND productoCodigo = :codigo")
    suspend fun eliminarPorCodigo(userId: String, codigo: String)

    // ✅ ACTUALIZADO: Filtrar por userId
    @Query("SELECT COUNT(*) FROM carrito_items WHERE userId = :userId")
    suspend fun obtenerCantidadTotal(userId: String): Int

    // ✅ ACTUALIZADO: Filtrar por userId
    @Query("SELECT SUM(cantidad) FROM carrito_items WHERE userId = :userId")
    suspend fun obtenerCantidadItems(userId: String): Int

    // ✅ ACTUALIZADO: Filtrar por userId
    @Query("DELETE FROM carrito_items WHERE userId = :userId")
    suspend fun limpiarCarritoUsuario(userId: String)

    // ✅ NUEVO: Limpiar carrito guest
    @Query("DELETE FROM carrito_items WHERE userId = 'guest'")
    suspend fun limpiarCarritoGuest()

    // ✅ NUEVO: Obtener todos los usuarios (para debug)
    @Query("SELECT DISTINCT userId FROM carrito_items")
    suspend fun obtenerTodosLosUsuarios(): List<String>
}