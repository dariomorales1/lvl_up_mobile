package cl.duoc.level_up_mobile.repository.carrito

import android.util.Log
import cl.duoc.level_up_mobile.data.remote.cart.CartApi
import cl.duoc.level_up_mobile.data.remote.cart.dto.AddItemRequestDto
import cl.duoc.level_up_mobile.data.remote.cart.dto.CartItemResponse
import cl.duoc.level_up_mobile.data.remote.cart.dto.CartResponse
import cl.duoc.level_up_mobile.data.remote.core.RetrofitClient
import cl.duoc.level_up_mobile.model.Cart
import cl.duoc.level_up_mobile.model.CartItem

class CartRepositoryRemote(
    private val api: CartApi = RetrofitClient.retrofit.create(CartApi::class.java)
) {

    private val TAG = "CartRepositoryRemote"

    // =============== MAPEO DTO → DOMINIO ===============

    private fun mapItem(dto: CartItemResponse): CartItem? {
        val productId = dto.productId ?: return null
        val productName = dto.productName ?: return null
        val unitPrice = dto.unitPrice ?: 0L
        val quantity = dto.quantity ?: 0

        return CartItem(
            id = dto.id?.toString(),
            productId = productId,
            productName = productName,
            unitPriceCents = unitPrice,
            quantity = quantity,
            imagenUrl = dto.imagenUrl
        )
    }

    private fun mapCart(dto: CartResponse?): Cart? {
        if (dto == null) return null
        val items = dto.items?.mapNotNull { mapItem(it) } ?: emptyList()
        val total = dto.totalAmount ?: items.sumOf { it.subtotalCents }

        return Cart(
            id = dto.id?.toString(),
            userId = dto.userId,
            items = items,
            totalAmountCents = total,
            updatedAt = dto.updatedAt?.toString()
        )
    }

    // =============== USUARIO AUTENTICADO ===============

    suspend fun getUserCart(userId: String): Cart? {
        return try {
            Log.d(TAG, "getUserCart: $userId")
            val res = api.getUserCart(userId)
            if (res.isSuccessful) {
                mapCart(res.body())
            } else {
                Log.e(TAG, "getUserCart error: code=${res.code()} body=${res.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "getUserCart exception", e)
            null
        }
    }

    suspend fun addItemToUserCart(userId: String, productId: String, quantity: Int): Cart? {
        return try {
            Log.d(TAG, "addItemToUserCart: user=$userId product=$productId qty=$quantity")
            val body = AddItemRequestDto(productId = productId, quantity = quantity)
            val res = api.addItemToUserCart(userId, body)
            if (res.isSuccessful) {
                mapCart(res.body())
            } else {
                Log.e(TAG, "addItemToUserCart error: code=${res.code()} body=${res.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "addItemToUserCart exception", e)
            null
        }
    }

    suspend fun updateUserItemQuantity(userId: String, productId: String, quantity: Int): Cart? {
        return try {
            Log.d(TAG, "updateUserItemQuantity: user=$userId product=$productId qty=$quantity")
            val res = api.updateUserItemQuantity(userId, productId, quantity)
            if (res.isSuccessful) {
                mapCart(res.body())
            } else {
                Log.e(TAG, "updateUserItemQuantity error: code=${res.code()} body=${res.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "updateUserItemQuantity exception", e)
            null
        }
    }

    suspend fun removeItemFromUserCart(userId: String, productId: String): Boolean {
        return try {
            Log.d(TAG, "removeItemFromUserCart: user=$userId product=$productId")
            val res = api.removeItemFromUserCart(userId, productId)
            res.isSuccessful
        } catch (e: Exception) {
            Log.e(TAG, "removeItemFromUserCart exception", e)
            false
        }
    }

    suspend fun clearUserCart(userId: String): Boolean {
        return try {
            Log.d(TAG, "clearUserCart: user=$userId")
            val res = api.clearUserCart(userId)
            res.isSuccessful
        } catch (e: Exception) {
            Log.e(TAG, "clearUserCart exception", e)
            false
        }
    }

    // =============== GUEST & MIGRACIÓN (si quisieras usarlo después) ===============

    suspend fun migrateGuestCartToUser(sessionId: String, userId: String): Cart? {
        return try {
            Log.d(TAG, "migrateGuestCartToUser: session=$sessionId user=$userId")
            val res = api.migrateGuestCartToUser(sessionId, userId)
            if (res.isSuccessful) {
                mapCart(res.body())
            } else {
                Log.e(TAG, "migrateGuestCartToUser error: code=${res.code()} body=${res.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "migrateGuestCartToUser exception", e)
            null
        }
    }
}
