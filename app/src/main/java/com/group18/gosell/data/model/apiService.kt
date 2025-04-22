package com.group18.gosell.data.model

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface apiService {
    @POST("/products")
    suspend fun addProduct(@Body product: Product): Response<String>

    @GET("/products")
    suspend fun getProducts(): List<Product>

    @GET("/products/{productId}")
    suspend fun getProductById(@Path("productId") productId: String): Response<Product>

    @GET("/users/products/{userId}")
    suspend fun getProductByUserId(@Path("userId") userId: String): List<Product>

    @POST("/favorites")
    suspend fun addWishList(@Body wishList: WishList): Response<String>

    @GET("/favorites/{userId}")
    suspend fun getUserWishList(@Path("userId") userId: String): List<WishList>

    @HTTP(method = "DELETE", path = "/favorites/{favoriteId}", hasBody = true)
    suspend fun removeWishList(@Path("favoriteId") favoriteId: String): Response<String>

    @POST("/users/register")
    suspend fun register(@Body user: User): Response<String>

    @GET("/users/{userId}")
    suspend fun getUserById(@Path("userId") userId: String): Response<User>

    @PUT("/products/{productId}")
    suspend fun updateProduct(@Path("productId") productId: String, @Body product: Product): Response<Product>

    @DELETE("/products/{productId}")
    suspend fun deleteProduct(@Path("productId") productId: String): Response<Unit>
}