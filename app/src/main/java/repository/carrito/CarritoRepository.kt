package cl.duoc.level_up_mobile.repository.carrito

import android.content.Context
import android.util.Log
import cl.duoc.level_up_mobile.model.CarritoItem
import cl.duoc.level_up_mobile.model.Producto
import cl.duoc.level_up_mobile.repository.database.AppDatabase
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class CarritoRepository(context: Context) {
    private val carritoDao = AppDatabase.getDatabase(context).carritoDao()
    private val auth = FirebaseAuth.getInstance()

    private fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: "guest"
    }

    suspend fun agregarProducto(producto: Producto, cantidad: Int = 1) {
        val userId = getCurrentUserId()
        val itemExistente = carritoDao.obtenerPorCodigo(userId, producto.codigo)

        if (itemExistente != null) {
            val nuevoItem = itemExistente.copy(
                cantidad = itemExistente.cantidad + cantidad
            )
            carritoDao.actualizar(nuevoItem)
        } else {
            val nuevoItem = CarritoItem(
                userId = userId,
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
        val userId = getCurrentUserId()
        val item = carritoDao.obtenerPorCodigo(userId, codigo)
        item?.let {
            if (nuevaCantidad > 0) {
                carritoDao.actualizar(it.copy(cantidad = nuevaCantidad))
            } else {
                carritoDao.eliminarPorCodigo(userId, codigo)
            }
        }
    }
    suspend fun eliminarProducto(codigo: String) {
        val userId = getCurrentUserId()
        carritoDao.eliminarPorCodigo(userId, codigo)
    }

    fun obtenerCarrito(): Flow<List<CarritoItem>> {
        val userId = getCurrentUserId()
        return carritoDao.obtenerPorUsuario(userId)
    }

    suspend fun obtenerCantidadTotal(): Int {
        val userId = getCurrentUserId()
        return carritoDao.obtenerCantidadTotal(userId)
    }

    suspend fun obtenerCantidadItems(): Int {
        val userId = getCurrentUserId()
        return carritoDao.obtenerCantidadItems(userId)
    }

    suspend fun limpiarCarrito() {
        val userId = getCurrentUserId()
        carritoDao.limpiarCarritoUsuario(userId)
    }

    suspend fun obtenerTotalCarrito(): Double {
        val userId = getCurrentUserId()
        val items = carritoDao.obtenerPorUsuario(userId).first()
        return items.sumOf { it.obtenerSubtotal() }
    }

    suspend fun transferirCarritoGuestAUsuario(userId: String) {
        try {
            val carritoGuest = carritoDao.obtenerPorUsuario("guest").first()

            if (carritoGuest.isNotEmpty()) {

                carritoGuest.forEach { itemGuest ->
                    val itemExistente = carritoDao.obtenerPorCodigo(userId, itemGuest.productoCodigo)

                    if (itemExistente != null) {
                        val nuevoItem = itemExistente.copy(
                            cantidad = itemExistente.cantidad + itemGuest.cantidad
                        )
                        carritoDao.actualizar(nuevoItem)
                    } else {
                        val nuevoItem = itemGuest.copy(
                            userId = userId,
                            id = 0
                        )
                        carritoDao.insertar(nuevoItem)
                    }
                }

                carritoDao.limpiarCarritoGuest()
            } else {
                Log.d("CarritoRepo", "No hay items en carrito guest para transferir")
            }
        } catch (e: Exception) {
            Log.e("CarritoRepo", "Error transfiriendo carrito: ${e.message}")
        }
    }

    suspend fun limpiarCarritoGuest() {
        carritoDao.limpiarCarritoGuest()
        Log.d("CarritoRepo", "Carrito guest limpiado")
    }

    suspend fun debugUsuarios() {
        try {
            val usuarios = carritoDao.obtenerTodosLosUsuarios()
            Log.d("CarritoRepo", "Usuarios con carrito: $usuarios")
        } catch (e: Exception) {
            Log.e("CarritoRepo", "Error en debug: ${e.message}")
        }
    }
}