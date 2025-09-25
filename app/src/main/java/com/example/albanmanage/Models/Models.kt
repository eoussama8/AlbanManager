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
            // Butter
            Product(context.getString(R.string.beurre_carton_25kg), 40.0, 5, R.drawable.beurre_cartom_25kg),

            // Packaging

            // Desserts
            Product(context.getString(R.string.flany_caramel_80g), 2.0, 24, R.drawable.flany_caramel80g),
            Product(context.getString(R.string.creme_dessert_chocolat_80g), 2.0, 24, R.drawable.creme_caramel80g),
            Product(context.getString(R.string.panna_cotta_fruits_rouges_80g), 2.5, 24, R.drawable.panna_cotta_fruits_rouges_80g),

            // Juices
            Product(context.getString(R.string.fresh_o_juice_orange_160g), 1.5, 24, R.drawable.fresh_fruits_jus_orange_160g),

            // Milk & Dairy
            Product(context.getString(R.string.lait_demiereme_albane_485g), 4.0, 12, R.drawable.produit1),
            Product(context.getString(R.string.product_lben_025l), 3.5, 12, R.drawable.produit4),
            Product(context.getString(R.string.leben_sachet_450g), 3.0, 12, R.drawable.leben_sachet_450g),
            Product(context.getString(R.string.raib_vanille_450g), 4.0, 12, R.drawable.raib_vanille_450g),
            Product(context.getString(R.string.lait_uht_entier_12_litre), 5.0, 12, R.drawable.lait_uht_entier_12_litre),

            // Smoothies & Drinks
            Product(context.getString(R.string.smoozy_pinacolada_440g), 6.0, 12, R.drawable.smoozy_pinacolada_440g),
            Product(context.getString(R.string.moniich_avocat_amande_440g), 6.5, 12, R.drawable.moniich_avocat_amande_440g),
            Product(context.getString(R.string.moniich_banane_sachet_215g), 3.0, 24, R.drawable.moniich_banane_sachet_215g),

            // Yogurts & Drinks
            Product(context.getString(R.string.piko_raibi_grenadine_75g), 1.5, 24, R.drawable.piko_raibi_grenadine_75g),
            Product(context.getString(R.string.piko_yaourt_boisson_vanille_140g), 2.0, 24, R.drawable.piko_yaourt_boisson_vanille_140g),
            Product(context.getString(R.string.yaourt_boisson_fraise_170g), 2.0, 24, R.drawable.yaourt_boisson_fraise_170g),
            Product(context.getString(R.string.raibi_grenadine_165g), 2.0, 24, R.drawable.raibi_grenadine_165g),
            Product(context.getString(R.string.moniich_carton_fraise_260g), 3.0, 12, R.drawable.moniich_carton_fraise_260g),
            Product(context.getString(R.string.nice_ferme_banane_110g), 1.5, 24, R.drawable.nice_ferme_banane_110g),
            Product(context.getString(R.string.yaourt_brosse_fraise_110g), 1.5, 24, R.drawable.yaourt_brosse_fraise_110g),
            Product(context.getString(R.string.piko_brosse_banane_60g), 1.0, 24, R.drawable.piko_brosse_banane_60g),
            Product(context.getString(R.string.fruits_brosse_cereales_110g), 2.0, 24, R.drawable.fruits_brosse_cereales_110g),
            Product(context.getString(R.string.grec_muesli_110g), 2.0, 24, R.drawable.grec_muesli_110g),
        )
    }
}