package cl.duoc.level_up_mobile.model

data class Cart(
    val id: String?,
    val userId: String?,
    val items: List<CartItem>,
    val totalAmountCents: Long,
    val updatedAt: String?
)
