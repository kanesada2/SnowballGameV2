package com.github.kanesada2.SnowballGame.api

import com.github.kanesada2.SnowballGame.config.BallConfig
import com.github.kanesada2.SnowballGame.item.BallItem
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

/**
 * Called when a player throws a ball.
 *
 * @param who The player who threw the ball
 */
class PlayerThrowBallEvent(
    who: Player
) : PlayerEvent(who), Cancellable {

    private var cancelled = false

    /**
     * The ball item in the player's hand
     */
    var itemBall: ItemStack? = null
        private set

    /**
     * The type of ball (extracted from item lore)
     */
    lateinit var ballType: String

    /**
     * The display name of the ball
     */
    lateinit var ballName: String

    /**
     * The initial throw velocity
     */
    lateinit var velocity: Vector

    /**
     * The spin vector applied to the ball
     */
    lateinit var spinVector: Vector

    /**
     * The acceleration value
     */
    var acceleration: Double = 0.0

    /**
     * Randomness factor for ball movement
     */
    var random: Double = 0.0

    /**
     * Particle effect tracker
     */
    var tracker: Particle? = null

    var trackerBlock: Material? = null

    /**
     * Reference point location
     */
    lateinit var rPoint: Location

    /**
     * Velocity modifier vector
     */
    lateinit var vModifier: Vector

    constructor(
        who: Player,
        hand: ItemStack,
        velocity: Vector,
        spinVector: Vector,
        acceleration: Double,
        random: Double,
        tracker: Particle?,
        trackerBlock: Material?,
        rPoint: Location,
        vModifier: Vector
    ) : this(who) {
        this.itemBall = hand
        this.ballType = BallItem.from(hand)?.ballType ?: "Normal"
        this.ballName = BallItem.from(hand)?.name ?: BallConfig.defaultName
        this.velocity = velocity
        this.spinVector = spinVector
        this.acceleration = acceleration
        this.random = random
        this.tracker = tracker
        this.trackerBlock = trackerBlock
        this.rPoint = rPoint
        this.vModifier = vModifier
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
