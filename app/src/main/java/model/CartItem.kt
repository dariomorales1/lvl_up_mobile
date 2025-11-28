package cl.duoc.level_up_mobile.model

data class CartItem(
    val id: String?,
    val productId: String,
    val productName: String,
    val unitPriceCents: Long,
    val quantity: Int,
    val imagenUrl: String?
) {
    val subtotalCents: Long
        get() = unitPriceCents * quantity
}
