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

        // 🔹 Promedio de puntuación desde las reseñas (Resena.puntuacion)
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

        // 🔹 Especificaciones (ProductSpecification.specification)
        val especificaciones = dto.especificaciones
            ?.mapNotNull { it.specification }
            ?: emptyList()

        // 🔹 Comentarios desde reseñas (opcional con nombre de usuario)
        val comentarios = dto.resenas
            ?.mapNotNull { resena ->
                resena.comentario?.let { comentario ->
                    val nombre = resena.usuarioNombre
                    if (!nombre.isNullOrBlank()) {
                        "$nombre: $comentario"
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

    // Cache simple en memoria para no pegarle al ms a cada rato
    private var cacheProductos: List<Producto>? = null

    private suspend fun getOrLoadProductos(): List<Producto> {
        // Si ya hay cache, la usamos
        cacheProductos?.let { return it }

        return try {
            val res = api.getAllProducts()
            if (res.isSuccessful) {
                val lista = res.body()
                    .orEmpty()
                    .mapNotNull { mapToDomain(it) }
                cacheProductos = lista
                lista
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // =========================
    // Métodos públicos para la UI
    // =========================

    /** Todos los productos del catálogo */
    suspend fun obtenerTodosLosProductos(): List<Producto> {
        return getOrLoadProductos()
    }

    /** Productos filtrados por categoría (case-insensitive) */
    suspend fun obtenerProductosPorCategoria(categoria: String): List<Producto> {
        val productos = getOrLoadProductos()
        return productos.filter { it.categoria.equals(categoria, ignoreCase = true) }
    }

    /** Todas las categorías distintas presentes en el catálogo */
    suspend fun obtenerTodasLasCategorias(): List<String> {
        val productos = getOrLoadProductos()
        return productos.map { it.categoria }.distinct()
    }

    /** Buscar productos por nombre / descripción corta / categoría */
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

    /**
     * “Destacados”: primer producto por categoría.
     * Es la misma lógica que usaste en el Home de React.
     */
    suspend fun obtenerProductosDestacados(): List<Producto> {
        val productos = getOrLoadProductos()

        return productos
            .groupBy { it.categoria }
            .mapNotNull { (_, lista) -> lista.firstOrNull() }
    }

    /** Por si alguna vez quieres refrescar a mano */
    fun limpiarCache() {
        cacheProductos = null
    }
}
