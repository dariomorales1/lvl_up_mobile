package cl.duoc.level_up_mobile.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "carrito_items")
data class CarritoItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val productoCodigo: String,
    val productoNombre: String,
    val productoPrecio: String,
    val productoImagenUrl: String,
    val cantidad: Int,
    val agregadoEn: Long = System.currentTimeMillis()
) {
    fun obtenerPrecioNumerico(): Double {
        return try {
            productoPrecio
                .replace(" CLP", "")
                .replace(".", "")
                .replace(",", ".")
                .toDouble()
        } catch (e: Exception) {
            0.0
        }
    }

    fun obtenerSubtotal(): Double {
        return obtenerPrecioNumerico() * cantidad
    }
}