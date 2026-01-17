package com.github.kanesada2.SnowballGame.task

import com.github.kanesada2.SnowballGame.api.UmpireCallEvent
import com.github.kanesada2.SnowballGame.config.MessageConfig
import com.github.kanesada2.SnowballGame.config.MessageType
import com.github.kanesada2.SnowballGame.extension.MetaKeys
import com.github.kanesada2.SnowballGame.extension.MetadatableExtension.getMeta
import com.github.kanesada2.SnowballGame.extension.MetadatableExtension.hasMeta
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.scheduler.BukkitRunnable


class BallJudgeTask(
    private val ball: Projectile,
    private val umpire: ArmorStand,
    private val minimumCorner: Location,
    private val maximumCorner: Location,
) : BukkitRunnable() {
    private var count: Int = 0

    override fun run() {
        count++
        if (ball.isDead || !ball.hasMeta(MetaKeys.MOVING_TYPE)) {
            this.cancel()
            return
        }
        if (count > 100) {
            this.cancel()
        }
        var i = 0.0
        while (i < 1) {
            // 時速144kmのボールは1tick(50ms)の間に2ブロック進むため、毎tickの判定では分解能が足りない。擬似的に1/10tick刻みで判定する
            if (ball.location.clone().add(ball.velocity.multiply(i)).toVector()
                    .isInAABB(minimumCorner.toVector(), maximumCorner.toVector())
            ) {
                val message = MessageConfig[MessageType.STRIKE]
                val speedStr = String.format("%.1f", ball.velocity.length() * 72) + "km/h"
                val typeStr = ball.getMeta(MetaKeys.MOVING_TYPE).toString()
                val playerStr = (ball.shooter as? Player)?.displayName?:"Dispenser"
                val msgString = message.format(
                    "SPEED" to speedStr,
                    "TYPE" to typeStr,
                    "PLAYER" to playerStr
                )
                val event = UmpireCallEvent(umpire, ball, msgString)
                Bukkit.getPluginManager().callEvent(event)
                if (event.isCancelled) {
                    this.cancel()
                    break
                }
                MessageConfig.broadcast(
                    MessageType.STRIKE,
                    umpire.location,
                    "SPEED" to speedStr,
                    "TYPE" to typeStr,
                    "PLAYER" to playerStr
                )
                this.cancel()
                break
            }
            i += 0.1
        }
    }
}