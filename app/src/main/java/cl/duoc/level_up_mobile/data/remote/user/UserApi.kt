package cl.duoc.level_up_mobile.data.remote.user

import cl.duoc.level_up_mobile.data.remote.user.dto.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*
import cl.duoc.level_up_mobile.data.remote.user.dto.DireccionRequest
import cl.duoc.level_up_mobile.data.remote.user.dto.DireccionResponse

interface UserApi {

    @POST("/users/public/register")
    suspend fun registerPublic(
        @Body body: UsuarioPublicRequest
    ): Response<UsuarioResponse>

    @GET("/users/me")
    suspend fun getCurrentUser(): Response<UsuarioResponse>

    @GET("/users/me/direcciones")
    suspend fun getMyAddresses(): Response<List<DireccionResponse>>

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

    @GET("/users/me/direcciones")
    suspend fun getDirecciones(
        @Header("Authorization") bearer: String
    ): Response<List<DireccionResponse>>

    @POST("/users/me/direcciones")
    suspend fun createDireccion(
        @Header("Authorization") bearer: String,
        @Body body: DireccionRequest
    ): Response<DireccionResponse>

    @PUT("/users/me/direcciones/{id}")
    suspend fun updateDireccion(
        @Path("id") id: Long,
        @Header("Authorization") bearer: String,
        @Body body: DireccionRequest
    ): Response<DireccionResponse>

    @DELETE("/users/me/direcciones/{id}")
    suspend fun deleteDireccion(
        @Path("id") id: Long,
        @Header("Authorization") bearer: String
    ): Response<Unit>

}
