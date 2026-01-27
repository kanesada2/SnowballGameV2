package com.github.kanesada2.SnowballGame.item

import com.github.kanesada2.SnowballGame.PersistentDataKeys
import com.github.kanesada2.SnowballGame.config.UmpireConfig
import com.github.kanesada2.SnowballGame.extension.hasPdc
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapelessRecipe
import org.bukkit.persistence.PersistentDataType

@JvmInline
value class UmpireItem(val item: ItemStack) : HasRecipe {
    companion object{
        fun from(item: ItemStack): UmpireItem? {
            return if (item.type == Material.QUARTZ_BLOCK && item.hasPdc(PersistentDataKeys.Umpire)) UmpireItem(item) else null
        }
        fun generate(name : String? = null): UmpireItem {
            val item = ItemStack(Material.QUARTZ_BLOCK)
            val meta = item.itemMeta
            val actualName = name ?: UmpireConfig.name
            meta?.setDisplayName(actualName)
            meta?.persistentDataContainer?.set(PersistentDataKeys.Base, PersistentDataType.BOOLEAN, true)
            meta?.persistentDataContainer?.set(PersistentDataKeys.Umpire, PersistentDataType.BOOLEAN, true)
            item.itemMeta = meta
            return UmpireItem(item)
        }
    }
    override fun recipe(key: NamespacedKey): ShapelessRecipe {
        return ShapelessRecipe(key, item).addIngredient(Material.QUARTZ_BLOCK).addIngredient(Material.OBSERVER)
    }
}