package cl.duoc.level_up_mobile.repository.user

import cl.duoc.level_up_mobile.data.remote.core.RetrofitClient
import cl.duoc.level_up_mobile.data.remote.user.UserApi
import cl.duoc.level_up_mobile.data.remote.user.dto.DireccionRequest
import cl.duoc.level_up_mobile.data.remote.user.dto.DireccionResponse
import cl.duoc.level_up_mobile.data.remote.user.dto.UsuarioRequest
import cl.duoc.level_up_mobile.data.remote.user.dto.UsuarioResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class UserRepository(
    private val api: UserApi = RetrofitClient.retrofit.create(UserApi::class.java)
) {

    suspend fun getCurrentUser(): UsuarioResponse? {
        return try {
            val res = api.getCurrentUser()
            if (res.isSuccessful) res.body() else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getMyAddresses(): List<DireccionResponse> {
        return try {
            val res = api.getMyAddresses()
            if (res.isSuccessful) res.body().orEmpty() else emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun createAddress(request: DireccionRequest): DireccionResponse? {
        return try {
            val res = api.createAddress(request)
            if (res.isSuccessful) res.body() else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun updateAddress(id: Long, request: DireccionRequest): DireccionResponse? {
        return try {
            val res = api.updateAddress(id, request)
            if (res.isSuccessful) res.body() else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun deleteAddress(id: Long): Boolean {
        return try {
            val res = api.deleteAddress(id)
            res.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun updateUser(id: String, request: UsuarioRequest): UsuarioResponse? {
        return try {
            val res = api.updateUser(id, request)
            if (res.isSuccessful) res.body() else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun uploadAvatar(id: String, file: File): UsuarioResponse? {
        return try {
            val requestFile = file
                .asRequestBody("image/jpeg".toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData(
                "file",
                file.name,
                requestFile
            )

            val res = api.uploadAvatar(id, part)
            if (res.isSuccessful) res.body() else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun deleteAvatar(id: String): UsuarioResponse? {
        return try {
            val res = api.deleteAvatar(id)
            if (res.isSuccessful) res.body() else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}