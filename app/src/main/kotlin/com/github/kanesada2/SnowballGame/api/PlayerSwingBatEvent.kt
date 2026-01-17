package com.github.kanesada2.SnowballGame.api

import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

/**
 * Called when a player swings a bat.
 *
 * @param who The player who swung the bat
 */
class PlayerSwingBatEvent(
    who: Player
) : PlayerEvent(who), Cancellable {

    private var cancelled = false

    /**
     * The bat item being swung
     */
    var bat: ItemStack? = null


    /**
     * The center location of the swing
     */
    lateinit var center: Location

    /**
     * The hit detection range vector
     */
    lateinit var hitRange: Vector

    /**
     * The force/power of the swing
     */
    var force: Float = 0f

    /**
     * Rate parameter for swing calculation
     */
    var rate: Double = 0.0

    /**
     * The bat movement vector
     */
    lateinit var batMove: Vector

    /**
     * Physics coefficient for the swing
     */
    var coefficient: Double = 0.0

    constructor(
        who: Player,
        bat: ItemStack?,
        center: Location,
        hitRange: Vector,
        force: Float,
        rate: Double,
        batMove: Vector,
        coefficient: Double
    ) : this(who) {
        this.bat = bat
        this.center = center
        this.hitRange = hitRange
        this.force = force
        this.rate = rate
        this.batMove = batMove
        this.coefficient = coefficient
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
