package com.github.kanesada2.SnowballGame.entity

import com.github.kanesada2.SnowballGame.api.PlayerSwingBatEvent
import com.github.kanesada2.SnowballGame.service.BallHitProcessor
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

@JvmInline
value class Bat(val entity: Entity) {

    companion object {

        /**
         * バットのエンティティを出現させる　実体は雪玉
         */
        fun spawn(location: Location): Bat {
            val entity = location.world!!.spawnEntity(location, EntityType.SNOWBALL).apply {
                setGravity(false)
            }
            return Bat(entity)
        }
    }

    val location: Location get() = entity.location

    fun findNearbyBalls(range: Vector): List<Ball> {
        return entity.world.getNearbyEntities(location, range.x, range.y, range.z) { it is Snowball && it != entity }
            .mapNotNull { Ball.from(it as Snowball) }
    }

    fun swing(
        player: Player,
        batItem: ItemStack? = null,
        force: Double = 1.0,
        rate: Double = 1.3,
        range: Vector,
        batMove: Vector,
        coefficient: Double = 1.0
    ): List<Ball> {
        val event = PlayerSwingBatEvent(
            who = player,
            bat = batItem,
            center = location,
            hitRange = range,
            force = force.toFloat(),
            rate = rate,
            batMove = batMove,
            coefficient = coefficient
        )
        Bukkit.getPluginManager().callEvent(event)
        if(event.isCancelled) return emptyList()
        val balls = findNearbyBalls(range)
        if (balls.isEmpty()) return emptyList()

        val attributes = SwingAttributes(
            center = event.center,
            hitRange = event.hitRange,
            force = event.force.toDouble(),
            rate = event.rate,
            batMove = event.batMove,
            coefficient = event.coefficient
        )

        return balls.map { ball ->
            BallHitProcessor.processHit(ball, attributes, player)
        }
    }

    fun remove() = entity.remove()
}