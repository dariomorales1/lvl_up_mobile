package cl.duoc.level_up_mobile.repository.carrito

import android.content.Context
import cl.duoc.level_up_mobile.model.CarritoItem
import cl.duoc.level_up_mobile.model.Producto
import cl.duoc.level_up_mobile.repository.database.AppDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class CarritoRepository(context: Context) {
    private val carritoDao = AppDatabase.getDatabase(context).carritoDao()

    suspend fun agregarProducto(producto: Producto, cantidad: Int = 1) {
        val itemExistente = carritoDao.obtenerPorCodigo(producto.codigo)

        if (itemExistente != null) {
            // Actualizar cantidad si ya existe
            val nuevoItem = itemExistente.copy(
                cantidad = itemExistente.cantidad + cantidad
            )
            carritoDao.actualizar(nuevoItem)
        } else {
            // Insertar nuevo item
            val nuevoItem = CarritoItem(
                productoCodigo = producto.codigo,
                productoNombre = producto.nombre,
                productoPrecio = producto.precio,
                productoImagenUrl = producto.imagenUrl,
                cantidad = cantidad
            )
            carritoDao.insertar(nuevoItem)
        }
    }

    suspend fun actualizarCantidad(codigo: String, nuevaCantidad: Int) {
        val item = carritoDao.obtenerPorCodigo(codigo)
        item?.let {
            if (nuevaCantidad > 0) {
                carritoDao.actualizar(it.copy(cantidad = nuevaCantidad))
            } else {
                carritoDao.eliminar(it)
            }
        }
    }

    suspend fun eliminarProducto(codigo: String) {
        carritoDao.eliminarPorCodigo(codigo)
    }

    fun obtenerCarrito(): Flow<List<CarritoItem>> {
        return carritoDao.obtenerTodos()
    }

    suspend fun obtenerCantidadTotal(): Int {
        return carritoDao.obtenerCantidadTotal()
    }

    suspend fun obtenerCantidadItems(): Int {
        return carritoDao.obtenerCantidadItems() ?: 0
    }

    suspend fun limpiarCarrito() {
        carritoDao.limpiarCarrito()
    }

    suspend fun obtenerTotalCarrito(): Double {
        val items = carritoDao.obtenerTodos().first()
        return items.sumOf { it.obtenerSubtotal() }
    }
}