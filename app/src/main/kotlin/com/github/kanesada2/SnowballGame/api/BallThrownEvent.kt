package com.github.kanesada2.SnowballGame.api

import org.bukkit.entity.Projectile
import org.bukkit.event.HandlerList
import org.bukkit.event.entity.EntityEvent

/**
 * Called when a ball is thrown.
 *
 * @param entity The projectile (ball) that was thrown
 */
class BallThrownEvent(
    entity: Projectile
) : EntityEvent(entity) {

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
