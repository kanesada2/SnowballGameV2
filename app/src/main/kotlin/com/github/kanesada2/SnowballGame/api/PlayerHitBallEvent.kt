package com.github.kanesada2.SnowballGame.api

import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent
import org.bukkit.util.Vector

/**
 * Called when a player hits a ball with a bat.
 *
 * @param who The player who hit the ball
 */
class PlayerHitBallEvent(
    who: Player,
    val beforeHit: Projectile
) : PlayerEvent(who), Cancellable {

    private var cancelled = false

    /**
     * The spin vector applied to the ball
     */
    var spinVector: Vector = Vector(0, 0, 0)

    /**
     * The velocity of the ball after being hit
     */
    var velocity: Vector = Vector(0,0,0)

    /**
     * The acceleration applied to the ball
     */
    var acceleration: Double = 0.0

    /**
     * Randomness factor for ball movement
     */
    var random: Double = 0.0

    /**
     * Particle effect tracker for the ball
     */
    var tracker: Particle? = null

    var trackerBlock: Material? = null

    constructor(
        who: Player,
        beforeHit: Projectile,
        spinVector: Vector,
        velocity: Vector,
        acceleration: Double,
        random: Double,
        tracker: Particle?,
        trackerBlock: Material?,
    ) : this(who, beforeHit) {
        this.spinVector = spinVector
        this.velocity = velocity
        this.acceleration = acceleration
        this.random = random
        this.tracker = tracker
        this.trackerBlock = trackerBlock
    }

    override fun isCancelled(): Boolean {
        return cancelled
    }

    override fun setCancelled(cancel: Boolean) {
        cancelled = cancel
    }

    override fun getHandlers(): HandlerList {
        return handlerList
    }

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}
