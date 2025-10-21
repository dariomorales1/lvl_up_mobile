package cl.duoc.level_up_mobile.repository.productos

import android.content.Context
import cl.duoc.level_up_mobile.model.Producto
import cl.duoc.level_up_mobile.model.ProductoJsonResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class JsonProductLoader(private val context: Context) {

    private val gson = Gson()

    fun cargarProductosDesdeJson(): List<Producto> {
        return try {
            val jsonString = context.assets.open("json/productos.json")
                .bufferedReader().use { it.readText() }

            val response = gson.fromJson(jsonString, ProductoJsonResponse::class.java)
            response.productos
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun obtenerProductoPorCodigo(codigo: String): Producto? {
        return cargarProductosDesdeJson().find { it.codigo == codigo }
    }

    fun obtenerProductosPorCategoria(categoria: String): List<Producto> {
        return cargarProductosDesdeJson().filter { it.categoria == categoria }
    }

    fun obtenerTodasLasCategorias(): List<String> {
        return cargarProductosDesdeJson().map { it.categoria }.distinct()
    }
}