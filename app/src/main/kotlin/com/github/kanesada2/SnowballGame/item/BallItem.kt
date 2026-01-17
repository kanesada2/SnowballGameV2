package com.github.kanesada2.SnowballGame.item

import com.github.kanesada2.SnowballGame.PersistentDataKeys
import com.github.kanesada2.SnowballGame.config.BallConfig
import com.github.kanesada2.SnowballGame.extension.getPdc
import com.github.kanesada2.SnowballGame.extension.hasPdc
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.persistence.PersistentDataType

@JvmInline
value class BallItem private constructor(val item: ItemStack): HasRecipe {
    val name: String
        get() = item.itemMeta?.displayName ?: BallConfig.defaultName
    val ballType: String?
        get() = item.getPdc(PersistentDataKeys.BallType, PersistentDataType.STRING)
    companion object {
        fun from(item: ItemStack): BallItem? {
            return if (item.type == Material.SNOWBALL && item.hasPdc(PersistentDataKeys.BallType)) BallItem(item) else null
        }
        fun generate(ballType: String, name : String? = null): BallItem {
            val item = ItemStack(Material.SNOWBALL)
            val actualName = name?: BallConfig.defaultName
            val meta = item.itemMeta
            meta?.setDisplayName(actualName)
            meta?.persistentDataContainer?.set(PersistentDataKeys.BallType, PersistentDataType.STRING, ballType)
            val coefficient = BallConfig.getCoefficient(ballType)
            if(coefficient != 1.0)
                if(meta?.hasLore()?: false){
                    meta?.lore?.add("Exit Velocity * $coefficient")
                }else{
                    meta?.lore = listOf("Exit Velocity * $coefficient")
                }

            item.itemMeta = meta
            return BallItem(item)
        }
    }
    override fun recipe(key: NamespacedKey): ShapedRecipe {
        return ShapedRecipe(key, item).apply {
            shape(
                "LSL",
                "LBL",
                "LSL"
            )
            val inclusion = when(ballType){
                "Highest" -> Material.ENDER_PEARL
                "Higher" -> Material.SLIME_BALL
                "Lower" -> Material.EGG
                "Lowest" -> Material.CLAY_BALL
                else -> Material.SNOWBALL
            }
            setIngredient('S', Material.STRING)
            setIngredient('L', Material.LEATHER)
            setIngredient('B', inclusion)
        }
    }
}