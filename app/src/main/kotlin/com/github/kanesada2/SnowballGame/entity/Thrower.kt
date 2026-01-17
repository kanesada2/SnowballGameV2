package com.github.kanesada2.SnowballGame.entity

import com.github.kanesada2.SnowballGame.api.PlayerThrowBallEvent
import com.github.kanesada2.SnowballGame.config.BallConfig
import com.github.kanesada2.SnowballGame.extension.BukkitRunnableExtension.later
import com.github.kanesada2.SnowballGame.extension.MetaKeys
import com.github.kanesada2.SnowballGame.extension.MetadatableExtension.hasMeta
import com.github.kanesada2.SnowballGame.extension.MetadatableExtension.setMeta
import com.github.kanesada2.SnowballGame.extension.getCenterLocation
import com.github.kanesada2.SnowballGame.item.BallItem
import com.github.kanesada2.SnowballGame.item.GloveItem
import com.github.kanesada2.SnowballGame.service.BallAttributes
import com.github.kanesada2.SnowballGame.service.BallAttributesCalculator
import com.github.kanesada2.SnowballGame.service.GloveAttributes
import com.github.kanesada2.SnowballGame.service.GloveAttributesCalculator
import com.github.kanesada2.SnowballGame.task.PlayerCoolDownTask
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.entity.Snowball
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.MainHand
import org.bukkit.util.Vector


@JvmInline
value class Thrower(val player: Player) {
    val isRightHanded: Boolean
        get() = player.mainHand == MainHand.RIGHT
    val mainHand: ItemStack
        get() = player.inventory.itemInMainHand
    companion object {
        fun from(player: Player): Thrower? {
            BallItem.from(player.inventory.itemInMainHand) ?: return null
            return Thrower(player)
        }
    }
    fun pitch(old: Projectile) {
        val releasePoint = old.location
        // もとの雪玉にはブレがあるので、そのまま使うと仰角が大きい時に回転の方向にブレが出る。ブレない視線の方を使う
        val velocity = player.eyeLocation.direction.normalize().multiply(old.velocity.length())
        val offHand = player.inventory.itemInOffHand

        // グラブ関連の値を適用
        val gloveItemAttributes = GloveItem.from(offHand)?.let{ glove ->
            GloveAttributesCalculator.calc(
                name = glove.name,
                player.eyeLocation,
                hasGloveOnLeft = isRightHanded
            )
        } ?: GloveAttributes()
        releasePoint.add(gloveItemAttributes.releasePointModifier)
        velocity.add(gloveItemAttributes.velocityModifier)
        val ballAttributes = BallItem.from(mainHand)?.let { ball ->
            BallAttributesCalculator.calc(
                ballName = ball.name,
                velocity = velocity,
                isRightHanded = isRightHanded,
                isFromDispenser = false
            )
        } ?: BallAttributes()

        launchWithEvent(mainHand, velocity, releasePoint, ballAttributes)
    }
    fun toss() {
        val eye: Location = player.eyeLocation
        val velocity: Vector = eye.getDirection().normalize()
        val zero = Vector(0, 0, 0)
        velocity.add(zero.clone().setY(0.5)).multiply(0.5)
        val releasePoint = eye.add(zero.clone().setY(-1))
        launchWithEvent(mainHand, velocity, releasePoint, BallAttributes())
    }

    fun placeOn(block: Block?) {
        if(block?.type != Material.BREWING_STAND) return
        val location = block.getCenterLocation().add(0.0,0.6,0.0)
        location.world?.getNearbyEntities(location, 0.5, 0.5, 0.5)
            ?.filterIsInstance<Snowball>()
            ?.firstNotNullOfOrNull{Ball.from(it)}?.let { return }
        if(player.gameMode != GameMode.CREATIVE) mainHand.amount -= 1
        val ball = Ball.launch(LaunchSettings(
            shooter = player,
            velocity = Vector(0.0, 0.0, 0.0),
            rPoint = location,
            ballType = BallItem.from(mainHand)?.ballType ?:"Normal"
        ))
        ball.projectile.setGravity(false)
    }

    private fun launchWithEvent(mainHand: ItemStack, velocity: Vector, releasePoint: Location, attributes: BallAttributes) {
        if(player.hasMeta(MetaKeys.PROHIBIT_THROWING)){
            player.sendMessage("You can't throw the next ball so quickly.")
            return
        }
        val (vModifier, spinVector, acceleration, random, tracker, trackerBlock) = attributes
        val throwEvent =
            PlayerThrowBallEvent(player, mainHand, velocity, spinVector, acceleration, random, tracker, trackerBlock, releasePoint, vModifier)
        Bukkit.getPluginManager().callEvent(throwEvent)
        if(throwEvent.isCancelled) return
        Ball.launch(LaunchSettings(
            shooter = throwEvent.player,
            hand = throwEvent.itemBall,
            isPitching = true,
            ballType = throwEvent.ballType,
            ballName = throwEvent.ballName,
            velocity = throwEvent.velocity,
            spinVector = throwEvent.spinVector,
            acceleration = throwEvent.acceleration,
            random = throwEvent.random,
            tracker = throwEvent.tracker,
            trackerBlock = throwEvent.trackerBlock,
            rPoint = throwEvent.rPoint,
            velocityModifier = throwEvent.vModifier
        ))
        player.setMeta(MetaKeys.PROHIBIT_THROWING, true)
        PlayerCoolDownTask(player).later(BallConfig.coolTime)
    }
}