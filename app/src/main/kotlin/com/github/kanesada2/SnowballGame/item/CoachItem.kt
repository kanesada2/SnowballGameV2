package com.github.kanesada2.SnowballGame.item

import com.github.kanesada2.SnowballGame.PersistentDataKeys
import com.github.kanesada2.SnowballGame.config.CoachConfig
import com.github.kanesada2.SnowballGame.extension.hasPdc
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapelessRecipe
import org.bukkit.persistence.PersistentDataType

@JvmInline
value class CoachItem(val item : ItemStack) : HasRecipe {
    companion object{
        fun from(item: ItemStack): CoachItem? {
            return if (item.type == Material.ARMOR_STAND && item.hasPdc(PersistentDataKeys.Coach)) CoachItem(item) else null
        }
        fun generate(name : String? = null): CoachItem {
            val item = ItemStack(Material.ARMOR_STAND)
            val meta = item.itemMeta
            val actualName = name ?: CoachConfig.name
            meta?.setDisplayName(actualName)
            meta?.persistentDataContainer?.set(PersistentDataKeys.Coach, PersistentDataType.BOOLEAN, true)
            item.itemMeta = meta
            return CoachItem(item)
        }
    }
    override fun recipe(key: NamespacedKey): ShapelessRecipe {
        return ShapelessRecipe(key, item).addIngredient(Material.ARMOR_STAND).addIngredient(Material.DISPENSER)
    }
}