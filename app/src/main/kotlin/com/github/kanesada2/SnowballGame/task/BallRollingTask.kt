package com.github.kanesada2.SnowballGame.task

import com.github.kanesada2.SnowballGame.config.BounceConfig
import org.bukkit.Particle
import org.bukkit.entity.Projectile
import org.bukkit.scheduler.BukkitRunnable


class BallRollingTask(private val ball: Projectile) : BukkitRunnable() {
    private var time = 0
    override fun run() {
        if (ball.isDead || time > 600) {
            this.cancel()
        }

        if (ball.velocity.length() < 0.05
            || BounceConfig.isPassthrough(ball.location.add(0.0, -0.15, 0.0).block)
            || BounceConfig.isAlwaysTop(
                ball.location.add(0.0, -1.0, 0.0).block)
                && !BounceConfig.isAlwaysTop(ball.location.add(0.0, -1.15, 0.0).block))
        {
            ball.setGravity(true)
            this.cancel()
        }
        time++
        ball.world.spawnParticle(Particle.ITEM_SNOWBALL, ball.location, 1)
        ball.velocity = ball.velocity.multiply(0.98)
    }
}