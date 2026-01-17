package com.github.kanesada2.SnowballGame.api

import org.bukkit.block.Block
import org.bukkit.entity.Projectile
import org.bukkit.event.HandlerList
import org.bukkit.event.entity.EntityEvent

/**
 * Called when a ball/projectile bounces off a block.
 *
 * @param entity The projectile (ball) that bounced
 */
class BallBounceEvent(
    entity: Projectile
) : EntityEvent(entity) {

    /**
     * The block the ball bounced on
     */
    var block: Block? = null

    /**
     * The projectile state before the bounce
     */
    lateinit var beforeBounce: Projectile
        private set

    /**
     * Whether this is the first bounce
     */
    var isFirst: Boolean = false

    constructor(entity: Projectile, block: Block, before: Projectile, first: Boolean) : this(entity) {
        this.block = block
        this.beforeBounce = before
        this.isFirst = first
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
