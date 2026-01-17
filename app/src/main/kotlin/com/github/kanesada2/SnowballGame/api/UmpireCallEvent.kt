package com.github.kanesada2.SnowballGame.api

import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Projectile
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList
import org.bukkit.event.entity.EntityEvent

/**
 * Called when an umpire (ArmorStand) makes a call.
 *
 * @param entity The umpire entity making the call
 */
class UmpireCallEvent(
    entity: ArmorStand
) : EntityEvent(entity), Cancellable {

    private var cancelled = false

    /**
     * The ball being judged
     */
    var ball: Projectile? = null

    /**
     * The umpire's call message
     */
    var msg: String? = null

    constructor(entity: ArmorStand, ball: Projectile, msg: String) : this(entity) {
        this.ball = ball
        this.msg = msg
    }

    override fun getEntity(): ArmorStand {
        return super.getEntity() as ArmorStand
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
