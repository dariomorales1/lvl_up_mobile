package cl.duoc.level_up_mobile.repository.carrito

import androidx.room.*
import cl.duoc.level_up_mobile.model.CarritoItem
import kotlinx.coroutines.flow.Flow

@Dao
interface CarritoDao {

    @Query("SELECT * FROM carrito_items WHERE userId = :userId ORDER BY agregadoEn DESC")
    fun obtenerPorUsuario(userId: String): Flow<List<CarritoItem>>

    @Query("SELECT * FROM carrito_items WHERE userId = :userId AND productoCodigo = :codigo")
    suspend fun obtenerPorCodigo(userId: String, codigo: String): CarritoItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(item: CarritoItem)

    @Update
    suspend fun actualizar(item: CarritoItem)

    @Delete
    suspend fun eliminar(item: CarritoItem)

    @Query("DELETE FROM carrito_items WHERE userId = :userId AND productoCodigo = :codigo")
    suspend fun eliminarPorCodigo(userId: String, codigo: String)

    @Query("SELECT COUNT(*) FROM carrito_items WHERE userId = :userId")
    suspend fun obtenerCantidadTotal(userId: String): Int

    @Query("SELECT SUM(cantidad) FROM carrito_items WHERE userId = :userId")
    suspend fun obtenerCantidadItems(userId: String): Int

    @Query("DELETE FROM carrito_items WHERE userId = :userId")
    suspend fun limpiarCarritoUsuario(userId: String)

    @Query("DELETE FROM carrito_items WHERE userId = 'guest'")
    suspend fun limpiarCarritoGuest()

    @Query("SELECT DISTINCT userId FROM carrito_items")
    suspend fun obtenerTodosLosUsuarios(): List<String>
}