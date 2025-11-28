// cl/duoc/level_up_mobile/data/remote/product/ProductApi.kt
package cl.duoc.level_up_mobile.data.remote.product

import cl.duoc.level_up_mobile.data.remote.product.dto.ProductResponse
import cl.duoc.level_up_mobile.data.remote.product.dto.ResenaResponse
import retrofit2.Response
import retrofit2.http.*

interface ProductApi {

    @GET("/products/")
    suspend fun getAllProducts(): Response<List<ProductResponse>>

    @GET("/products/{productCode}")
    suspend fun getProduct(
        @Path("productCode") productCode: String
    ): Response<ProductResponse>

    @GET("/products/{productCode}/resenas")
    suspend fun getResenasByProduct(
        @Path("productCode") productCode: String
    ): Response<List<ResenaResponse>>
}
