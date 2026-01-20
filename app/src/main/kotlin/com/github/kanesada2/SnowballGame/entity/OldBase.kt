package com.github.kanesada2.SnowballGame.entity

import com.github.kanesada2.SnowballGame.Constants
import com.github.kanesada2.SnowballGame.extension.getCenterLocation
import com.github.kanesada2.SnowballGame.item.BaseItem
import com.github.kanesada2.SnowballGame.item.UmpireItem
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.ArmorStand

/**
 * 旧バージョンで既に設置済みのBaseを安全に削除するためだけに存在する
 */
@JvmInline
value class OldBase (override val stand: ArmorStand): WithBlock{
    companion object{
        fun from(stand: ArmorStand): OldBase? {
            return if(stand.equipment?.boots?.itemMeta?.lore?.contains(Constants.Misc.OLD_ITEM_MARKER_LORE)?:false) OldBase(stand) else null
        }
        fun from(block: Block?): OldBase? {
            if(block?.type != Material.QUARTZ_SLAB && block?.type != Material.QUARTZ_BLOCK) return null
            val entities = block.world.getNearbyEntities(block.getCenterLocation(), 1.0, 1.0, 1.0)
            entities.filterIsInstance<ArmorStand>().firstNotNullOfOrNull { from(it) }?.let { return it }
            return null
        }
    }
}