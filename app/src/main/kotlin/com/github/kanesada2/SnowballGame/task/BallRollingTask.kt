package com.github.kanesada2.SnowballGame.task

import com.github.kanesada2.SnowballGame.Constants
import com.github.kanesada2.SnowballGame.config.BounceConfig
import org.bukkit.Particle
import org.bukkit.entity.Projectile
import org.bukkit.scheduler.BukkitRunnable


class BallRollingTask(private val ball: Projectile) : BukkitRunnable() {
    private var time = 0
    override fun run() {
        if (ball.isDead || time > Constants.BallRolling.MAX_ROLLING_TIME) {
            this.cancel()
        }

        if (ball.velocity.length() < Constants.BallRolling.STOP_VELOCITY_THRESHOLD
            || BounceConfig.isPassthrough(ball.location.add(0.0, Constants.BallRolling.SURFACE_CHECK_OFFSET_SHALLOW, 0.0).block)
            || BounceConfig.isAlwaysTop(ball.location.add(0.0, Constants.BallRolling.SURFACE_CHECK_OFFSET_ONE_BLOCK, 0.0).block)
            && !BounceConfig.isAlwaysTop(ball.location.add(0.0, Constants.BallRolling.SURFACE_CHECK_OFFSET_DEEP, 0.0).block))
        {
            ball.setGravity(true)
            this.cancel()
        }
        time++
        ball.world.spawnParticle(Particle.ITEM_SNOWBALL, ball.location, 1)
        ball.velocity = ball.velocity.multiply(Constants.BallRolling.ROLLING_FRICTION)
    }
}