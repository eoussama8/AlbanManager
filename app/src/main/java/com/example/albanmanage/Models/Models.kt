package com.example.albanmanage.Models

import android.content.Context
import com.example.albanmanage.R

data class Product(
    val name: String,
    val price: Double,
    val pack: Int,
    val imageResId: Int // Resource ID for drawable
)

object ProductRepository {
    fun getProducts(context: Context): List<Product> {
        return listOf(
            Product(context.getString(R.string.product_lait_05l), 4.0, 24, R.drawable.produit1),
            Product(context.getString(R.string.product_croissance), 8.0, 9, R.drawable.produit2),
            Product(context.getString(R.string.product_smoozy), 4.0, 24, R.drawable.produit3),
            Product(context.getString(R.string.product_lben_025l), 2.5, 15, R.drawable.produit4),
            Product(context.getString(R.string.product_raibi), 2.5, 24, R.drawable.produit5),
            Product(context.getString(R.string.product_lben_05l), 5.0, 12, R.drawable.produit6),
            Product(context.getString(R.string.product_danon), 2.5, 24, R.drawable.produit7)
        )
    }
}