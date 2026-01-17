package com.github.kanesada2.SnowballGame.entity

import com.github.kanesada2.SnowballGame.config.BallConfig
import com.github.kanesada2.SnowballGame.extension.MetaKeys
import com.github.kanesada2.SnowballGame.extension.MetadatableExtension.getMeta
import com.github.kanesada2.SnowballGame.extension.MetadatableExtension.hasMeta
import com.github.kanesada2.SnowballGame.extension.MetadatableExtension.removeMeta
import com.github.kanesada2.SnowballGame.extension.MetadatableExtension.setMeta
import com.github.kanesada2.SnowballGame.item.BallItem
import com.github.kanesada2.SnowballGame.service.BallAttributesCalculator
import org.bukkit.Material
import org.bukkit.block.BlockFace.*
import org.bukkit.block.Dispenser
import org.bukkit.block.data.Directional
import org.bukkit.entity.Projectile
import org.bukkit.inventory.ItemStack

@JvmInline
value class PitchingMachine (val dispenser: Dispenser){
    val item: ItemStack?
        get() = dispenser.getMeta(MetaKeys.DISPENSING_ITEM)
    companion object{
        fun from(dispenser: Dispenser) : PitchingMachine?{
            return if(BallConfig.enabled && dispenser.hasMeta(MetaKeys.DISPENSING_ITEM)) PitchingMachine(dispenser) else null
        }
        fun mark(dispenser: Dispenser, item: ItemStack): PitchingMachine?{
            if(!BallConfig.enabled || item.type != Material.SNOWBALL || BallItem.from(item) == null) return null
            dispenser.setMeta(MetaKeys.DISPENSING_ITEM, item)
            return PitchingMachine(dispenser)
        }
    }

    fun pitch(projectile: Projectile) {
        val data = dispenser.block.blockData as? Directional ?: return
        val ball = item?.let{BallItem.from(it)}?: return
        val directionalLocation = dispenser.location
        when(data.facing){
            SOUTH -> directionalLocation.yaw = 0f
            WEST -> directionalLocation.yaw = 90f
            NORTH -> directionalLocation.yaw = 180f
            EAST -> directionalLocation.yaw = 270f
            else -> return
        }
        // ディスペンサーからの投球はコントロールが悪すぎるので補正する
        val speed = projectile.velocity.length()
        // 補正用にまっすぐ向いているベクトルを用意
        val facingVector = dispenser.block.getRelative(data.facing).location
                .subtract(directionalLocation).toVector()
                .apply {
                    normalize()
                    y  = 0.05 // 全体的に低いので
                    multiply(speed * 2) // こちらを2倍優先させるため
                }
        // 速度も遅いので補正
        val velocity = projectile.velocity.add(facingVector).normalize().multiply(speed * 1.4)
        val (vModifier, spinVector, acceleration, random, tracker, trackerBlock) = BallAttributesCalculator.calc(
            ballName = ball.name,
            velocity = velocity,
            isRightHanded = true,
            isFromDispenser = true
        )
        Ball.launch(LaunchSettings(
            shooter = dispenser.blockProjectileSource!!,
            velocity = velocity,
            rPoint = projectile.location,
            isPitching = true,
            ballType = ball.ballType!!,
            ballName = ball.name,
            velocityModifier = vModifier,
            spinVector = spinVector,
            acceleration = acceleration,
            random = random,
            tracker = tracker,
            trackerBlock = trackerBlock,
        ))
        // 元の雪玉は消す
        projectile.remove()
        // 投げ終わったら今投げたボールの情報は消す
        dispenser.removeMeta(MetaKeys.DISPENSING_ITEM)
    }
}