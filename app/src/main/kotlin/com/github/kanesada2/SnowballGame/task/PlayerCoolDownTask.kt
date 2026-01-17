package com.github.kanesada2.SnowballGame.task

import com.github.kanesada2.SnowballGame.extension.MetaKeys
import com.github.kanesada2.SnowballGame.extension.MetadatableExtension.hasMeta
import com.github.kanesada2.SnowballGame.extension.MetadatableExtension.removeMeta
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable


class PlayerCoolDownTask(private val player: Player) : BukkitRunnable() {
    override fun run() {
        if (player.hasMeta(MetaKeys.PROHIBIT_THROWING)) {
            player.removeMeta(MetaKeys.PROHIBIT_THROWING)
        }
        if (player.hasMeta(MetaKeys.PROHIBIT_SLIDING)) {
            player.removeMeta(MetaKeys.PROHIBIT_SLIDING)
        }
        if(player.hasMeta(MetaKeys.PROHIBIT_CALLING_KNOCK)) {
            player.removeMeta(MetaKeys.PROHIBIT_CALLING_KNOCK)
        }
        if(player.hasMeta(MetaKeys.PROHIBIT_PROGRESS_SWING)) {
            player.removeMeta(MetaKeys.PROHIBIT_PROGRESS_SWING)
        }
    }
}