package cl.duoc.level_up_mobile.data.remote.cart.dto

data class CartResponse(
    val id: String?,                  // UUID en string
    val userId: String?,
    val items: List<CartItemResponse>?,
    val totalAmount: Long?,           // en centavos
    val updatedAt: String?            // ISO datetime
)