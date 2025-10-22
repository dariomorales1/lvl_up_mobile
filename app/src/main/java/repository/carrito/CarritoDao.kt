package cl.duoc.level_up_mobile.repository.carrito

import androidx.room.*
import cl.duoc.level_up_mobile.model.CarritoItem
import kotlinx.coroutines.flow.Flow

@Dao
interface CarritoDao {

    @Query("SELECT * FROM carrito_items ORDER BY agregadoEn DESC")
    fun obtenerTodos(): Flow<List<CarritoItem>>

    @Query("SELECT * FROM carrito_items WHERE productoCodigo = :codigo")
    suspend fun obtenerPorCodigo(codigo: String): CarritoItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(item: CarritoItem)

    @Update
    suspend fun actualizar(item: CarritoItem)

    @Delete
    suspend fun eliminar(item: CarritoItem)

    @Query("DELETE FROM carrito_items WHERE productoCodigo = :codigo")
    suspend fun eliminarPorCodigo(codigo: String)

    @Query("SELECT COUNT(*) FROM carrito_items")
    suspend fun obtenerCantidadTotal(): Int

    @Query("SELECT SUM(cantidad) FROM carrito_items")
    suspend fun obtenerCantidadItems(): Int

    @Query("DELETE FROM carrito_items")
    suspend fun limpiarCarrito()
}