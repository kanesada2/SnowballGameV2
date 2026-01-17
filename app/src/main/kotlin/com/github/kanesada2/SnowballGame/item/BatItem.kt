package com.github.kanesada2.SnowballGame.item

import com.github.kanesada2.SnowballGame.PersistentDataKeys
import com.github.kanesada2.SnowballGame.config.BatConfig
import com.github.kanesada2.SnowballGame.config.BatValues
import com.github.kanesada2.SnowballGame.extension.hasPdc
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.persistence.PersistentDataType

@JvmInline
value class BatItem private constructor(val item: ItemStack) : HasRecipe {
    val name: String
        get() = item.itemMeta?.displayName ?: BatConfig.name
    private val config: BatValues?
        get() = BatConfig[name]

    val range: Double get() = config?.range ?: 1.2
    val fly: Double get() = config?.fly ?: 0.0
    val length: Double get() = config?.length ?: 3.0
    val power: Double get() = config?.power ?: 1.0
    companion object{
        fun from(item: ItemStack): BatItem? {
            return if (item.type == Material.BOW && item.hasPdc(PersistentDataKeys.Bat)) BatItem(item) else null
        }
        fun generate(name : String? = null): BatItem {
            val item = ItemStack(Material.BOW)
            val actualName = name?: BatConfig.name
            val meta = item.itemMeta
            meta?.setDisplayName(actualName)
            meta?.persistentDataContainer?.set(PersistentDataKeys.Bat, PersistentDataType.BOOLEAN, true)
            item.itemMeta = meta
            return BatItem(item)
        }
    }
    override fun recipe(key: NamespacedKey): ShapedRecipe {
        return ShapedRecipe(key, item).apply {
            shape(
                "  S",
                " S ",
                "S  "
            )
            setIngredient('S', Material.STICK)
        }
    }
}