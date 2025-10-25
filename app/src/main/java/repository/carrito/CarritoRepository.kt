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

    // ✅ Obtener el userId actual (logueado o guest)
    private fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: "guest"
    }

    suspend fun agregarProducto(producto: Producto, cantidad: Int = 1) {
        val userId = getCurrentUserId()
        val itemExistente = carritoDao.obtenerPorCodigo(userId, producto.codigo)

        if (itemExistente != null) {
            // Actualizar cantidad si ya existe
            val nuevoItem = itemExistente.copy(
                cantidad = itemExistente.cantidad + cantidad
            )
            carritoDao.actualizar(nuevoItem)
            Log.d("CarritoRepo", "Producto actualizado: ${producto.nombre} para usuario: $userId")
        } else {
            // Insertar nuevo item con userId
            val nuevoItem = CarritoItem(
                userId = userId,
                productoCodigo = producto.codigo,
                productoNombre = producto.nombre,
                productoPrecio = producto.precio,
                productoImagenUrl = producto.imagenUrl,
                cantidad = cantidad
            )
            carritoDao.insertar(nuevoItem)
            Log.d("CarritoRepo", "Producto agregado: ${producto.nombre} para usuario: $userId")
        }
    }

    suspend fun actualizarCantidad(codigo: String, nuevaCantidad: Int) {
        val userId = getCurrentUserId()
        val item = carritoDao.obtenerPorCodigo(userId, codigo)
        item?.let {
            if (nuevaCantidad > 0) {
                carritoDao.actualizar(it.copy(cantidad = nuevaCantidad))
                Log.d("CarritoRepo", "Cantidad actualizada: $codigo a $nuevaCantidad para usuario: $userId")
            } else {
                carritoDao.eliminarPorCodigo(userId, codigo)
                Log.d("CarritoRepo", "Producto eliminado: $codigo para usuario: $userId")
            }
        }
    }

    suspend fun eliminarProducto(codigo: String) {
        val userId = getCurrentUserId()
        carritoDao.eliminarPorCodigo(userId, codigo)
        Log.d("CarritoRepo", "Producto eliminado: $codigo para usuario: $userId")
    }

    fun obtenerCarrito(): Flow<List<CarritoItem>> {
        val userId = getCurrentUserId()
        Log.d("CarritoRepo", "Obteniendo carrito para usuario: $userId")
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
        Log.d("CarritoRepo", "Carrito limpiado para usuario: $userId")
    }

    suspend fun obtenerTotalCarrito(): Double {
        val userId = getCurrentUserId()
        val items = carritoDao.obtenerPorUsuario(userId).first()
        return items.sumOf { it.obtenerSubtotal() }
    }

    // ✅ TRANSFERIR carrito de guest a usuario al hacer login
    suspend fun transferirCarritoGuestAUsuario(userId: String) {
        try {
            val carritoGuest = carritoDao.obtenerPorUsuario("guest").first()

            if (carritoGuest.isNotEmpty()) {
                Log.d("CarritoRepo", "Transferiendo ${carritoGuest.size} items de guest a usuario: $userId")

                // Transferir cada item del guest al usuario
                carritoGuest.forEach { itemGuest ->
                    val itemExistente = carritoDao.obtenerPorCodigo(userId, itemGuest.productoCodigo)

                    if (itemExistente != null) {
                        // Si ya existe, sumar las cantidades
                        val nuevoItem = itemExistente.copy(
                            cantidad = itemExistente.cantidad + itemGuest.cantidad
                        )
                        carritoDao.actualizar(nuevoItem)
                        Log.d("CarritoRepo", "Item fusionado: ${itemGuest.productoNombre}")
                    } else {
                        // Si no existe, crear nuevo item con el userId
                        val nuevoItem = itemGuest.copy(
                            userId = userId,
                            id = 0 // Nueva ID auto-generada
                        )
                        carritoDao.insertar(nuevoItem)
                        Log.d("CarritoRepo", "Item transferido: ${itemGuest.productoNombre}")
                    }
                }

                // Limpiar carrito guest después de transferir
                carritoDao.limpiarCarritoGuest()
                Log.d("CarritoRepo", "Carrito transferido exitosamente")
            } else {
                Log.d("CarritoRepo", "No hay items en carrito guest para transferir")
            }
        } catch (e: Exception) {
            Log.e("CarritoRepo", "Error transfiriendo carrito: ${e.message}")
        }
    }

    // ✅ Limpiar carrito guest (para logout)
    suspend fun limpiarCarritoGuest() {
        carritoDao.limpiarCarritoGuest()
        Log.d("CarritoRepo", "Carrito guest limpiado")
    }

    // ✅ Debug: ver todos los usuarios con carrito
    suspend fun debugUsuarios() {
        try {
            val usuarios = carritoDao.obtenerTodosLosUsuarios()
            Log.d("CarritoRepo", "Usuarios con carrito: $usuarios")
        } catch (e: Exception) {
            Log.e("CarritoRepo", "Error en debug: ${e.message}")
        }
    }
}