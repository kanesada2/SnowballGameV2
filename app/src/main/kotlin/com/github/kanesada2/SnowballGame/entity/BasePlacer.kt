package com.github.kanesada2.SnowballGame.entity

import com.github.kanesada2.SnowballGame.PersistentDataKeys
import com.github.kanesada2.SnowballGame.config.BaseConfig
import com.github.kanesada2.SnowballGame.config.UmpireConfig
import com.github.kanesada2.SnowballGame.extension.getCenterLocation
import com.github.kanesada2.SnowballGame.extension.setPdc
import com.github.kanesada2.SnowballGame.item.BaseItem
import com.github.kanesada2.SnowballGame.item.UmpireItem
import org.bukkit.block.Block
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

@JvmInline
value class BasePlacer(val player: Player) {
    val mainHand: ItemStack
        get() = player.inventory.itemInMainHand
    companion object{
        fun from(player: Player): BasePlacer? {
            val mainHand = player.inventory.itemInMainHand
            BaseItem.from(mainHand)?.let {
                return BasePlacer(player)
            }
            return null
        }
    }
    fun place(block: Block) {
        val isUmpire = UmpireItem.from(mainHand) != null // umpireはbaseでもある
        val yOffset = if (isUmpire) 0.5 else 0.0
        val defaultName = if (isUmpire) UmpireConfig.name else BaseConfig.name

        val location = block.getCenterLocation().add(0.0, yOffset, 0.0)

        block.world.spawn(location, ArmorStand::class.java).apply {
            setGravity(false)
            isMarker = true
            isCollidable = false
            isCustomNameVisible = true
            isInvulnerable = true
            isVisible = false
            customName = mainHand.itemMeta?.displayName ?: defaultName

            setPdc(PersistentDataKeys.Base, PersistentDataType.BOOLEAN, true)
            if (isUmpire) {
                setPdc(PersistentDataKeys.Umpire, PersistentDataType.BOOLEAN, true)
            }
        }
    }
}