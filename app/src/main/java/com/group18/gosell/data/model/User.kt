package com.group18.gosell.data.model
import java.util.Date

data class User(
    val id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val dateOfBirth: Date? = null,
    val emailVerified: Boolean = false,
    val createDate: Long = System.currentTimeMillis(),
    val address: String? = null,
    val avatar: String? = null,
    val wishlist: List<String> = emptyList() // List of product IDs in wishlist
) {
    constructor() : this("", "", "", "", null, false, 0L, null, null, emptyList())
}