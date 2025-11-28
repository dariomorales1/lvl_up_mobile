// cl/duoc/level_up_mobile/data/remote/product/dto/ProductResponse.kt
package cl.duoc.level_up_mobile.data.remote.product.dto

data class ProductResponse(
    val id: Int?,
    val codigo: String?,
    val nombre: String?,
    val descripcionCorta: String?,
    val descripcionLarga: String?,
    val categoria: String?,
    val precio: Double?,
    val stock: Int?,
    val imagenUrl: String?,
    val especificaciones: List<ProductSpecificationResponse>?,
    val resenas: List<ResenaResponse>?
)




