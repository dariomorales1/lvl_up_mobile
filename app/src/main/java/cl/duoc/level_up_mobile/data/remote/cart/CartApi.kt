package cl.duoc.level_up_mobile.data.remote.cart

import cl.duoc.level_up_mobile.data.remote.cart.dto.AddItemRequestDto
import cl.duoc.level_up_mobile.data.remote.cart.dto.CartResponse
import retrofit2.Response
import retrofit2.http.*

interface CartApi {

    // ========= USUARIOS AUTENTICADOS =========

    @GET("carts/user/{userId}")
    suspend fun getUserCart(
        @Path("userId") userId: String
    ): Response<CartResponse>

    @POST("carts/user/{userId}/items")
    suspend fun addItemToUserCart(
        @Path("userId") userId: String,
        @Body body: AddItemRequestDto
    ): Response<CartResponse>

    @PUT("carts/user/{userId}/items/{productId}")
    suspend fun updateUserItemQuantity(
        @Path("userId") userId: String,
        @Path("productId") productId: String,
        @Query("quantity") quantity: Int
    ): Response<CartResponse>

    @DELETE("carts/user/{userId}/items/{productId}")
    suspend fun removeItemFromUserCart(
        @Path("userId") userId: String,
        @Path("productId") productId: String
    ): Response<Unit>

    @DELETE("carts/user/{userId}/clear")
    suspend fun clearUserCart(
        @Path("userId") userId: String
    ): Response<Unit>

    // ========= USUARIOS ANÓNIMOS (GUEST) =========

    @GET("carts/guest/{sessionId}")
    suspend fun getGuestCart(
        @Path("sessionId") sessionId: String
    ): Response<CartResponse>

    @POST("carts/guest/{sessionId}/items")
    suspend fun addItemToGuestCart(
        @Path("sessionId") sessionId: String,
        @Body body: AddItemRequestDto
    ): Response<CartResponse>

    @PUT("carts/guest/{sessionId}/items/{productId}")
    suspend fun updateGuestItemQuantity(
        @Path("sessionId") sessionId: String,
        @Path("productId") productId: String,
        @Query("quantity") quantity: Int
    ): Response<CartResponse>

    @DELETE("carts/guest/{sessionId}/items/{productId}")
    suspend fun removeItemFromGuestCart(
        @Path("sessionId") sessionId: String,
        @Path("productId") productId: String
    ): Response<Unit>

    @DELETE("carts/guest/{sessionId}/clear")
    suspend fun clearGuestCart(
        @Path("sessionId") sessionId: String
    ): Response<Unit>

    // ========= MIGRACIÓN GUEST → USER =========

    @POST("carts/migrate/{sessionId}/to/{userId}")
    suspend fun migrateGuestCartToUser(
        @Path("sessionId") sessionId: String,
        @Path("userId") userId: String
    ): Response<CartResponse>

    // ========= HEALTH =========

    @GET("carts/health")
    suspend fun health(): Response<String>
}
