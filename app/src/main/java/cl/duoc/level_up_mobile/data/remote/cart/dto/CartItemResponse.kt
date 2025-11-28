package cl.duoc.level_up_mobile.data.remote.cart.dto

import java.util.UUID

data class CartItemResponse(
    val id: String?,          // UUID en string
    val productId: String?,
    val productName: String?,
    val unitPrice: Long?,     // en centavos
    val quantity: Int?,
    val imagenUrl: String?
)