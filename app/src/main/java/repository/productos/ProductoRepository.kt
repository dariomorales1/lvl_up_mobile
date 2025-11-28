// cl/duoc/level_up_mobile/repository/productos/ProductoRepository.kt
package cl.duoc.level_up_mobile.repository.productos

import cl.duoc.level_up_mobile.data.remote.core.RetrofitClient
import cl.duoc.level_up_mobile.data.remote.product.ProductApi
import cl.duoc.level_up_mobile.data.remote.product.dto.ProductResponse
import cl.duoc.level_up_mobile.model.Producto
import java.text.NumberFormat
import java.util.Locale

class ProductoRepository(
    private val api: ProductApi = RetrofitClient.retrofit.create(ProductApi::class.java)
) {

    private fun formatPrecio(precio: Double?): String {
        if (precio == null) return "$0"
        val nf = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
        return nf.format(precio)
    }

    private fun mapToDomain(dto: ProductResponse): Producto? {
        val codigo = dto.codigo ?: return null
        val nombre = dto.nombre ?: return null
        val descripcionCorta = dto.descripcionCorta ?: ""
        val descripcionLarga = dto.descripcionLarga ?: ""
        val categoria = dto.categoria ?: "Sin categoría"
        val imagenUrl = dto.imagenUrl ?: ""
        val precio = formatPrecio(dto.precio)

        val puntuacion = if (!dto.resenas.isNullOrEmpty()) {
            val ratings = dto.resenas.mapNotNull { it.rating?.toDouble() }
            if (ratings.isNotEmpty()) {
                val avg = ratings.average()
                String.format(Locale.US, "%.1f", avg)
            } else {
                "5.0"
            }
        } else {
            "5.0"
        }

        return Producto(
            codigo = codigo,
            nombre = nombre,
            descripcionCorta = descripcionCorta,
            descripcionLarga = descripcionLarga,
            categoria = categoria,
            imagenUrl = imagenUrl,
            precio = precio,
            puntuacion = puntuacion
        )
    }

    // ========== MÉTODOS PÚBLICOS QUE USARÁ LA UI ==========

    suspend fun obtenerTodosLosProductos(): List<Producto> {
        return try {
            val res = api.getAllProducts()
            if (res.isSuccessful) {
                res.body()
                    .orEmpty()
                    .mapNotNull { mapToDomain(it) }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun obtenerProductosPorCategoria(categoria: String): List<Producto> {
        val todos = obtenerTodosLosProductos()
        return todos.filter { it.categoria.equals(categoria, ignoreCase = true) }
    }

    suspend fun obtenerTodasLasCategorias(): List<String> {
        val todos = obtenerTodosLosProductos()
        return todos.map { it.categoria }.distinct()
    }
}
