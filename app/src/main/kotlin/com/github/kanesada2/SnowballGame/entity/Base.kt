package com.github.kanesada2.SnowballGame.entity


import com.github.kanesada2.SnowballGame.PersistentDataKeys
import com.github.kanesada2.SnowballGame.config.BaseConfig
import com.github.kanesada2.SnowballGame.config.MessageConfig
import com.github.kanesada2.SnowballGame.config.MessageType
import com.github.kanesada2.SnowballGame.config.UmpireConfig
import com.github.kanesada2.SnowballGame.extension.getCenterLocation
import com.github.kanesada2.SnowballGame.extension.hasPdc
import com.github.kanesada2.SnowballGame.item.BaseItem
import com.github.kanesada2.SnowballGame.item.UmpireItem
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player

@JvmInline
value class Base(val stand: ArmorStand) {
    companion object {
        fun from(stand: ArmorStand): Base? {
            if(!BaseConfig.enabled) return null
            return if(stand.hasPdc(PersistentDataKeys.Base)) Base(stand) else null
        }
        fun from(block: Block?): Base? {
            if(block?.type != Material.QUARTZ_SLAB && block?.type != Material.QUARTZ_BLOCK) return null
            val entities = block.world.getNearbyEntities(block.getCenterLocation(), 1.0, 1.0, 1.0)
            entities.filterIsInstance<ArmorStand>().firstNotNullOfOrNull { from(it) }?.let { return it }
            return null
        }
    }

    fun standingOn(player: Player): Boolean {
        val topEdge = stand.location.clone().add(0.5, 0.5, 0.5).toVector()
        val bottomEdge = stand.location.clone().add(-0.5, -0.5, -0.5).toVector()
        return player.location.toVector().isInAABB(bottomEdge, topEdge)
    }

    fun handleLeftClick(player: Player) : Boolean{
        if(!standingOn(player)) return false
        val customName = stand.customName ?: if(Umpire.from(stand) != null) UmpireConfig.name else BaseConfig.name
        Batter.from(player)?.let {
            MessageConfig.broadcast(MessageType.REACH_BASE, stand.location,
                "PLAYER" to player.displayName,
                "BASE" to customName
            )
            return true
        }
        Fielder.from(player)?.let {
            MessageConfig.broadcast(MessageType.STANDING_BASE, stand.location,
                "PLAYER" to player.displayName,
                "BASE" to customName
            )
            return true
        }
        return false
    }

    fun breakWithBlock(block: Block){
        val item = if(block.type == Material.QUARTZ_BLOCK) UmpireItem.generate(stand.customName).item
        else if((block.type == Material.QUARTZ_SLAB)) BaseItem.generate(stand.customName).item
        else return
        stand.remove()
        block.world.dropItemNaturally(block.location, item)
    }
}