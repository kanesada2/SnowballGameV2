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

interface WithBlock {
    val stand: ArmorStand
    fun breakWithBlock(block: Block){
        val item = if(block.type == Material.QUARTZ_BLOCK) UmpireItem.generate(stand.customName).item
        else if((block.type == Material.QUARTZ_SLAB)) BaseItem.generate(stand.customName).item
        else return
        stand.remove()
        block.world.dropItemNaturally(block.location, item)
    }
}
@JvmInline
value class Base(override val stand: ArmorStand) : WithBlock {
    companion object {
        fun from(stand: ArmorStand): Base? {
            if(!BaseConfig.enabled) return null
            return if(stand.hasPdc(PersistentDataKeys.Base)) Base(stand) else null
        }
        fun from(block: Block?): Base? {
            if(block?.type != Material.QUARTZ_SLAB && block?.type != Material.QUARTZ_BLOCK) return null
            val entities = block.world.getNearbyEntities(block.getCenterLocation(), 1.0, 1.0, 1.0)
            entities.filterIsInstance<ArmorStand>().firstNotNullOfOrNull { Base.Companion.from(it) }?.let { return it }
            return null
        }
    }

    fun standingOn(player: Player): Boolean {
        val topEdge = stand.location.clone().add(0.5, 0.5, 0.5).toVector()
        val bottomEdge = stand.location.clone().add(-0.5, -0.5, -0.5).toVector()
        return player.location.toVector().isInAABB(bottomEdge, topEdge)
    }

    fun handleLeftClick(role: CanSlide) {
        val customName = stand.customName ?: if(Umpire.from(stand) != null) UmpireConfig.name else BaseConfig.name
        if(!standingOn(role.player)) return
        val messageType =
            when (role) {
                is Batter -> MessageType.REACH_BASE
                is Fielder -> MessageType.STANDING_BASE
                else -> return
            }
        MessageConfig.broadcast(messageType, stand.location, "PLAYER" to role.player.displayName, "BASE" to customName)
    }
}