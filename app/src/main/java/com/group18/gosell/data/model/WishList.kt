package com.group18.gosell.data.model

data class WishList (
    val userId: String = "",
    val productId: String = "",
    val favoriteId: String = ""
){
    constructor(): this("", "", "")
}