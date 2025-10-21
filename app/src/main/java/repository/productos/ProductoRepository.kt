package cl.duoc.level_up_mobile.repository.productos

import android.content.Context
import cl.duoc.level_up_mobile.model.Producto

class ProductoRepository(private val context: Context) {

    private val jsonLoader = JsonProductLoader(context)

    fun obtenerTodosLosProductos(): List<Producto> {
        return jsonLoader.cargarProductosDesdeJson()
    }

    fun obtenerProductosDestacados(): List<Producto> {
        // Usa los primeros 8 productos como destacados
        return jsonLoader.cargarProductosDesdeJson().take(8)
    }

    fun obtenerProductoPorCodigo(codigo: String): Producto? {
        return jsonLoader.obtenerProductoPorCodigo(codigo)
    }

    fun obtenerProductosPorCategoria(categoria: String): List<Producto> {
        return jsonLoader.obtenerProductosPorCategoria(categoria)
    }

    fun obtenerTodasLasCategorias(): List<String> {
        return jsonLoader.obtenerTodasLasCategorias()
    }

    fun buscarProductos(query: String): List<Producto> {
        val productos = jsonLoader.cargarProductosDesdeJson()
        return productos.filter {
            it.nombre.contains(query, ignoreCase = true) ||
                    it.descripcionCorta.contains(query, ignoreCase = true) ||
                    it.categoria.contains(query, ignoreCase = true)
        }
    }
}