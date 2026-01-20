package com.github.kanesada2.SnowballGame.entity

import com.github.kanesada2.SnowballGame.Constants
import com.github.kanesada2.SnowballGame.api.PlayerCatchBallEvent
import com.github.kanesada2.SnowballGame.config.BallConfig
import com.github.kanesada2.SnowballGame.config.BaseConfig
import com.github.kanesada2.SnowballGame.config.MessageConfig
import com.github.kanesada2.SnowballGame.config.MessageType
import com.github.kanesada2.SnowballGame.config.UmpireConfig
import com.github.kanesada2.SnowballGame.extension.BukkitRunnableExtension.later
import com.github.kanesada2.SnowballGame.extension.MetaKeys
import com.github.kanesada2.SnowballGame.extension.MetadatableExtension.setMeta
import com.github.kanesada2.SnowballGame.item.BallItem
import com.github.kanesada2.SnowballGame.item.GloveItem
import com.github.kanesada2.SnowballGame.service.GloveAttributes
import com.github.kanesada2.SnowballGame.service.GloveAttributesCalculator
import com.github.kanesada2.SnowballGame.task.PlayerCoolDownTask
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.MainHand
import org.bukkit.util.Vector

@JvmInline
value class Fielder(override val player: Player) : CanSlide{
    val isBallHolder : Boolean
        get() = BallItem.from(player.inventory.itemInMainHand) != null
    val isReadyToCatch: Boolean
        get() = player.inventory.itemInMainHand.type == Material.AIR
    val isRightHanded: Boolean
        get() = player.mainHand == MainHand.RIGHT
    companion object {
        fun from(player: Player): Fielder? {
            GloveItem.from(player.inventory.itemInOffHand)?: return null
            return Fielder(player)
        }
        fun mark(player: Player) : Fielder {
            return Fielder(player)
        }
    }
    fun tryCatch(from: Location? = null, range: Vector? = null, rate: Double = Constants.Fielder.DEFAULT_CATCH_RATE): Boolean{
        val gloveItemAttributes = GloveItem.from(player.inventory.itemInOffHand)?.let {
            GloveAttributesCalculator.calc(
                name = it.name,
                eye = player.eyeLocation,
                hasGloveOnLeft = this.isRightHanded
            )
        }?: GloveAttributes()

        val actualRange = range?: gloveItemAttributes.catchRange
        val actualFrom = from?: player.location
        // 捕球を試したら5tickの間投球できない
        player.setMeta(MetaKeys.PROHIBIT_THROWING, true)
        PlayerCoolDownTask(player).later(Constants.Fielder.CATCH_COOLDOWN_TICKS)

        val ball = player.world.getNearbyEntities(
            player.eyeLocation,
            actualRange.x,
            actualRange.y,
            actualRange.z
        ) { it is Snowball }
            .mapNotNull { Ball.from(it as Snowball) }
            .minByOrNull { actualFrom.distance(it.projectile.location) }  // 一番近いのを取る
            ?: return false

        val distance = actualFrom.distance(ball.projectile.location)
        val catchRate = actualRange.x * actualRange.y * actualRange.z * rate

        if (Math.random() * catchRate > distance) {
            ball.projectile.remove()
            val event = catch(ball)
            ball.removeMetaData()
            if (!event.isCancelled) {
                player.sendMessage("Caught!")
                return true
            }
        }

        player.sendMessage("Missed!")
        ball.projectile.apply {
            setGravity(true)
            velocity = velocity.multiply(Math.random()).add(Vector.getRandom().multiply(Constants.Fielder.MISS_SCATTER_FACTOR))
        }
        return false
    }

    fun catch(ball: Ball): PlayerCatchBallEvent{
        val inventory: Inventory = player.inventory
        val itemBall: ItemStack = BallItem.generate(ball.ballType, ball.nameForDrop).item
        val catchEvent = PlayerCatchBallEvent(
            player,
            ball.projectile,
            itemBall,
            ball.isInFlight
        )
        if (inventory.containsAtLeast(itemBall, 1) || inventory.firstEmpty() != -1) {

            Bukkit.getPluginManager().callEvent(catchEvent)
            if (!catchEvent.isCancelled) {
                player.inventory.addItem(catchEvent.itemBall)
            }
        } else {
            catchEvent.isCancelled = true
            ball.projectile.world.dropItem(ball.projectile.location, itemBall)
        }

        return catchEvent
    }

    fun tag(runner: Player) {
        if(!isBallHolder) return
        MessageConfig.broadcast(MessageType.TAG, player.location, "PLAYER" to player.displayName, "RUNNER" to runner.displayName)
    }

    fun appeal(){
        if(!isBallHolder) return
        player.getNearbyEntities(0.5, 0.5, 0.5)
            .filterIsInstance<ArmorStand>()
            .firstNotNullOfOrNull { Base.from(it) }
            ?.let {
                val name = it.stand.customName ?: if(Umpire.from(it.stand) != null) UmpireConfig.name else BaseConfig.name
                MessageConfig.broadcast(MessageType.TOUCH_BASE, player.location, "PLAYER" to player.displayName, "BASE" to name)
            }
    }
}