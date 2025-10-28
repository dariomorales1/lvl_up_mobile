package cl.duoc.level_up_mobile.model

import androidx.annotation.DrawableRes
import cl.duoc.level_up_mobile.R
import com.google.gson.annotations.SerializedName

data class Producto(
    @SerializedName("Código") val codigo: String,
    @SerializedName("Nombre") val nombre: String,
    @SerializedName("Precio") val precio: String,
    @SerializedName("Descripción Corta") val descripcionCorta: String,
    @SerializedName("Descripción Larga") val descripcionLarga: String,
    @SerializedName("Categoría") val categoria: String,
    @SerializedName("Stock") val stock: String,
    @SerializedName("Especificaciones") val especificaciones: List<String>,
    @SerializedName("Puntuacion") val puntuacion: String,
    @SerializedName("Comentarios") val comentarios: List<String>,
    @SerializedName("imgLink") val imagenUrl: String
)
data class ProductoJsonResponse(
    val productos: List<Producto>
)