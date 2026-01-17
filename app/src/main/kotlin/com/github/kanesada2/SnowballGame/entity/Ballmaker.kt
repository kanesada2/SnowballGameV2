package com.github.kanesada2.SnowballGame.entity

import com.github.kanesada2.SnowballGame.PersistentDataKeys
import com.github.kanesada2.SnowballGame.config.BallConfig
import com.github.kanesada2.SnowballGame.config.BallmakerConfig
import com.github.kanesada2.SnowballGame.extension.getPdc
import com.github.kanesada2.SnowballGame.extension.hasPdc
import com.github.kanesada2.SnowballGame.extension.removePdc
import com.github.kanesada2.SnowballGame.extension.setPdc
import com.github.kanesada2.SnowballGame.item.BallItem
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Snowman
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

@JvmInline
value class Ballmaker(val snowman: Snowman) {
    val ballType: String?
        get() = snowman.getPdc(PersistentDataKeys.BallType, PersistentDataType.STRING)
    companion object {
        fun from(snowman: Snowman): Ballmaker? {
            return if(BallmakerConfig.enabled && snowman.hasPdc(PersistentDataKeys.BallType)) Ballmaker(snowman) else null
        }
        fun mark(snowman: Snowman, item: ItemStack): Ballmaker? {
            if (!BallConfig.enabled || item.type != Material.SNOWBALL) return null
            val ballItem = BallItem.from(item) ?: return null
            if(snowman.hasPdc(PersistentDataKeys.BallType)) snowman.removePdc(PersistentDataKeys.BallType)
            snowman.setPdc(PersistentDataKeys.BallType, PersistentDataType.STRING, ballItem.ballType!!)
            snowman.isCustomNameVisible = true
            snowman.customName = BallmakerConfig.name
            return Ballmaker(snowman)
        }
    }

    fun breakAndDrop(block: Block){
        if(block.type != Material.SNOW) return
        if(ballType == null) return
        val item = BallItem.generate(ballType!!).item
        block.world.dropItemNaturally(snowman.location, item)
    }
}