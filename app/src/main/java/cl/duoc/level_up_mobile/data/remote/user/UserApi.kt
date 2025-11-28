package cl.duoc.level_up_mobile.data.remote.user

import cl.duoc.level_up_mobile.data.remote.user.dto.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface UserApi {

    @POST("/users/public/register")
    suspend fun registerPublic(
        @Body body: UsuarioPublicRequest
    ): Response<UsuarioResponse>

    @GET("/users/me")
    suspend fun getCurrentUser(): Response<UsuarioResponse>

    @GET("/users/me/direcciones")
    suspend fun getMyAddresses(): Response<List<DireccionResponse>>

    @POST("/users/me/direcciones")
    suspend fun createAddress(
        @Body body: DireccionRequest
    ): Response<DireccionResponse>

    @PUT("/users/me/direcciones/{id}")
    suspend fun updateAddress(
        @Path("id") id: Long,
        @Body body: DireccionRequest
    ): Response<DireccionResponse>

    @DELETE("/users/me/direcciones/{id}")
    suspend fun deleteAddress(
        @Path("id") id: Long
    ): Response<Void>

    @PUT("/users/{id}")
    suspend fun updateUser(
        @Path("id") id: String,
        @Body body: UsuarioRequest
    ): Response<UsuarioResponse>

    @Multipart
    @POST("/users/{id}/avatar")
    suspend fun uploadAvatar(
        @Path("id") id: String,
        @Part file: MultipartBody.Part
    ): Response<UsuarioResponse>

    @DELETE("/users/{id}/avatar")
    suspend fun deleteAvatar(
        @Path("id") id: String
    ): Response<UsuarioResponse>
}