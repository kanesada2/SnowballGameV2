package com.github.kanesada2.SnowballGame.item

import com.github.kanesada2.SnowballGame.PersistentDataKeys
import com.github.kanesada2.SnowballGame.config.BaseConfig
import com.github.kanesada2.SnowballGame.extension.hasPdc
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapelessRecipe
import org.bukkit.persistence.PersistentDataType

@JvmInline
value class BaseItem(val item: ItemStack): HasRecipe {
    companion object{
        fun from(item: ItemStack): BaseItem? {
            return if ((item.type == Material.QUARTZ_SLAB || item.type == Material.QUARTZ_BLOCK)
                        && item.hasPdc(PersistentDataKeys.Base)) BaseItem(item) else null
        }
        fun generate(name: String? = null): BaseItem {
            val item = ItemStack(Material.QUARTZ_SLAB)
            val meta = item.itemMeta
            val actualName = name ?: BaseConfig.name
            meta?.setDisplayName(actualName)
            meta?.persistentDataContainer?.set(PersistentDataKeys.Base, PersistentDataType.BOOLEAN, true)
            item.itemMeta = meta
            return BaseItem(item)
        }
    }
    override fun recipe(key: NamespacedKey): ShapelessRecipe {
        return ShapelessRecipe(key, item).addIngredient(Material.QUARTZ_SLAB).addIngredient(Material.OBSERVER)
    }
}