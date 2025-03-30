package com.group18.gosell.data.model

data class Product(
    var id: String = "",
    val name: String = "",
    val description: String? = null,
    val place: String? = null,
    val sellerId: String = "",
    val image: String? = null,
    val type: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long? = null
) {
    constructor() : this("", "", null, null, "", null, null, 0L, null)
}