package com.github.kanesada2.SnowballGame.api

import org.bukkit.entity.Projectile
import org.bukkit.event.HandlerList
import org.bukkit.event.entity.EntityEvent

/**
 * Called when a ball/projectile hits something (generic hit event).
 *
 * @param entity The projectile (ball) that hit something
 */
class BallHitEvent(
    entity: Projectile
) : EntityEvent(entity) {

    /**
     * The projectile state before the hit
     */
    var beforeHit: Projectile? = null
        private set

    constructor(entity: Projectile, before: Projectile) : this(entity) {
        this.beforeHit = before
    }

    override fun getEntity(): Projectile {
        return super.getEntity() as Projectile
    }

    override fun getHandlers(): HandlerList {
        return handlerList
    }

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}
