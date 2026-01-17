package com.github.kanesada2.SnowballGame.api

import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent
import org.bukkit.inventory.ItemStack

/**
 * Called when a player catches a ball.
 *
 * @param who The player who caught the ball
 */
class PlayerCatchBallEvent(
    who: Player
) : PlayerEvent(who), Cancellable {

    private var cancelled = false

    /**
     * The ball (projectile) being caught
     */
    lateinit var ball: Projectile

    /**
     * The ball as an item stack
     */
    lateinit var itemBall: ItemStack

    /**
     * Whether this is a direct catch (caught in flight)
     */
    var isDirect: Boolean = false

    constructor(who: Player, ball: Projectile, itemBall: ItemStack, isDirect: Boolean) : this(who) {
        this.ball = ball
        this.itemBall = itemBall
        this.isDirect = isDirect
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
