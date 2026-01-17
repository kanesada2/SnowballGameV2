package com.github.kanesada2.SnowballGame.entity

import com.github.kanesada2.SnowballGame.config.BallConfig
import com.github.kanesada2.SnowballGame.config.BroadcastMessage
import com.github.kanesada2.SnowballGame.extension.BukkitRunnableExtension.later
import com.github.kanesada2.SnowballGame.extension.MetaKeys
import com.github.kanesada2.SnowballGame.extension.MetadatableExtension.hasMeta
import com.github.kanesada2.SnowballGame.extension.MetadatableExtension.setMeta
import com.github.kanesada2.SnowballGame.task.PlayerCoolDownTask
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable

interface CanSlide{
    val player: Player
    fun slide() {
        if(player.hasMeta(MetaKeys.PROHIBIT_SLIDING)) return
        // 恐らくは移動が挟まる関係で、一瞬後で実行してやる必要がある
        object : BukkitRunnable() {
            override fun run() {
                player.velocity = player.location.getDirection().normalize().multiply(1.2).setY(0)
                // スライディング後しばらくは飛べず、ボールも投げられない
                player.setMeta(MetaKeys.PROHIBIT_THROWING, true)
                player.setMeta(MetaKeys.PROHIBIT_SLIDING, true)
                PlayerCoolDownTask(player).later(BallConfig.coolTime)
                val newFacing = player.location
                newFacing.pitch = 60f
                player.teleport(newFacing)
                val message = BroadcastMessage(message = "[[DARK_AQUA]][[BOLD]]*** YOU ARE TRYING TO DIVE! ***")
                player.sendMessage(message.format())
                player.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, 10, 128))
            }
        }.later(1)
    }
}