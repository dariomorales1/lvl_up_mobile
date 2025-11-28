package cl.duoc.level_up_mobile.repository.productos

import android.util.Log
import cl.duoc.level_up_mobile.data.remote.core.RetrofitClient
import cl.duoc.level_up_mobile.data.remote.product.ProductApi
import cl.duoc.level_up_mobile.data.remote.product.dto.ProductResponse
import cl.duoc.level_up_mobile.model.Producto
import java.text.NumberFormat
import java.util.Locale

class ProductoRepository(
    private val api: ProductApi = RetrofitClient.retrofit.create(ProductApi::class.java)
) {

    private val TAG = "ProductoRepository"

    // =========================
    // Helpers privados
    // =========================

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
            val valores = dto.resenas.mapNotNull { it.puntuacion?.toDouble() }
            if (valores.isNotEmpty()) {
                val avg = valores.average()
                String.format(Locale.US, "%.1f", avg)
            } else {
                "5.0"
            }
        } else {
            "5.0"
        }

        val especificaciones = dto.especificaciones
            ?.mapNotNull { it.specification }
            ?: emptyList()

        val comentarios = dto.resenas
            ?.mapNotNull { resena ->
                resena.comentario?.let { comentario ->
                    val nombreUser = resena.usuarioNombre
                    if (!nombreUser.isNullOrBlank()) {
                        "$nombreUser: $comentario"
                    } else {
                        comentario
                    }
                }
            }
            ?: emptyList()

        return Producto(
            codigo = codigo,
            nombre = nombre,
            descripcionCorta = descripcionCorta,
            descripcionLarga = descripcionLarga,
            categoria = categoria,
            imagenUrl = imagenUrl,
            precio = precio,
            puntuacion = puntuacion,
            especificaciones = especificaciones,
            stock = dto.stock ?: 0,
            comentarios = comentarios
        )
    }

    // Cache simple en memoria
    private var cacheProductos: List<Producto>? = null

    private suspend fun getOrLoadProductos(): List<Producto> {
        cacheProductos?.let {
            Log.d(TAG, "Usando cache: ${it.size} productos")
            return it
        }

        return try {
            Log.d(TAG, "Llamando a GET /products/ ...")
            val res = api.getAllProducts()
            Log.d(TAG, "Respuesta HTTP: ${res.code()}")

            if (res.isSuccessful) {
                val body = res.body()
                Log.d(TAG, "Body recibido: ${body?.size ?: 0} productos")
                val lista = body
                    .orEmpty()
                    .mapNotNull { mapToDomain(it) }
                Log.d(TAG, "Mapeados a dominio: ${lista.size} productos")
                cacheProductos = lista
                lista
            } else {
                Log.e(TAG, "Error en respuesta: code=${res.code()}, errorBody=${res.errorBody()?.string()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción al llamar a productos", e)
            emptyList()
        }
    }

    // =========================
    // Métodos públicos para la UI
    // =========================

    suspend fun obtenerTodosLosProductos(): List<Producto> {
        return getOrLoadProductos()
    }

    suspend fun obtenerProductosPorCategoria(categoria: String): List<Producto> {
        val productos = getOrLoadProductos()
        return productos.filter { it.categoria.equals(categoria, ignoreCase = true) }
    }

    suspend fun obtenerTodasLasCategorias(): List<String> {
        val productos = getOrLoadProductos()
        return productos.map { it.categoria }.distinct()
    }

    suspend fun buscarProductos(query: String): List<Producto> {
        if (query.isBlank()) return emptyList()
        val q = query.trim().lowercase()
        val productos = getOrLoadProductos()
        return productos.filter { p ->
            p.nombre.lowercase().contains(q) ||
                    p.descripcionCorta.lowercase().contains(q) ||
                    p.categoria.lowercase().contains(q)
        }
    }

    suspend fun obtenerProductosDestacados(): List<Producto> {
        val productos = getOrLoadProductos()
        val destacados = productos
            .groupBy { it.categoria }
            .mapNotNull { (_, lista) -> lista.firstOrNull() }
        Log.d(TAG, "Destacados: ${destacados.size} productos")
        return destacados
    }

    fun limpiarCache() {
        cacheProductos = null
    }
}
