package com.github.kanesada2.SnowballGame.service

import com.github.kanesada2.SnowballGame.config.GloveConfig
import org.bukkit.Location
import org.bukkit.util.Vector

data class GloveAttributes(
    val releasePointModifier: Vector = Vector(0.0, 0.0, 0.0),
    val velocityModifier: Vector = Vector(0.0, 0.0, 0.0),
    val catchRange: Vector = Vector(3.0, 3.0, 3.0),
)

object GloveAttributesCalculator {
    fun calc(
        name: String,
        eye: Location,
        hasGloveOnLeft: Boolean
    ): GloveAttributes {
        val moveDirection = if(hasGloveOnLeft) 1 else -1
        val config = GloveConfig[name]
        val releasePointModifier = Vector(0.0, config?.vertical ?: 0.0, 0.0)
        // 向いている向きの真横を取る
        eye.yaw += 90
        val horizontalOffset = (0.2 + (config?.horizontal ?: 0.0)) * moveDirection
        releasePointModifier.add(eye.direction.normalize().multiply(horizontalOffset))
        // 腕の高さを変えるとリリースでボールを叩く向きが変わる、の感じを再現(前/後で離す、は影響しない)
        val velocityModifier = releasePointModifier.clone().multiply(-0.1)
        // 向いている向きに戻す
        eye.yaw -= 90
        releasePointModifier.add(eye.direction.normalize().multiply(config?.closeness ?: 0.0))
        return GloveAttributes(
            releasePointModifier = releasePointModifier,
            velocityModifier = velocityModifier,
            catchRange = Vector(1,1,1).multiply(config?.catchRange ?: 3.0),
        )
    }
}