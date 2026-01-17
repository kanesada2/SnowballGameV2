package com.github.kanesada2.SnowballGame.item

import com.github.kanesada2.SnowballGame.PersistentDataKeys
import com.github.kanesada2.SnowballGame.config.GloveConfig
import com.github.kanesada2.SnowballGame.extension.hasPdc
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.persistence.PersistentDataType

@JvmInline
value class GloveItem private constructor(val item: ItemStack) : HasRecipe {
    val name: String
        get() = item.itemMeta?.displayName ?: ""
    companion object {
        fun from(item: ItemStack): GloveItem? {
            return if (item.type == Material.LEATHER && item.hasPdc(PersistentDataKeys.Glove)) GloveItem(item) else null
        }
        fun generate(name : String? = null): CoachItem {
            val item = ItemStack(Material.LEATHER)
            val meta = item.itemMeta
            val actualName = name ?: GloveConfig.name
            meta?.setDisplayName(actualName)
            meta?.persistentDataContainer?.set(PersistentDataKeys.Glove, PersistentDataType.BOOLEAN, true)
            item.itemMeta = meta
            return CoachItem(item)
        }
    }

    override fun recipe(key: NamespacedKey): ShapedRecipe {
        return ShapedRecipe(key, item).apply {
            shape(
                "LLL",
                "LLL",
                " L "
            )
            setIngredient('L', Material.LEATHER)
        }
    }
}