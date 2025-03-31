package com.group18.gosell.data.model
import java.text.NumberFormat
import java.util.Locale

data class Product(
    var id: String = "",
    val name: String = "",
    val description: String? = null,
    val place: String? = null,
    val sellerId: String = "",
    val image: String? = null,
    val type: String? = null,
    val price: Double? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long? = null
) {
    constructor() : this("", "", null, null, "", null, null,null,0L, null)
}

fun formatPrice(price: Double?): String {
    return if (price != null && price >= 0) {
        NumberFormat.getCurrencyInstance(Locale.getDefault()).format(price)
    } else {
        "Free"
    }
}