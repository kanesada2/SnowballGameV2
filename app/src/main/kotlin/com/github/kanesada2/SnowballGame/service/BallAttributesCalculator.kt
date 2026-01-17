package com.github.kanesada2.SnowballGame.service

import com.github.kanesada2.SnowballGame.config.BallConfig
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.util.Vector
import kotlin.math.cos
import kotlin.math.sign
import kotlin.math.sin

data class BallAttributes(
    val velocityModifier: Vector = Vector(0.0, 0.0, 0.0),
    val spinVector: Vector = Vector(0.0, 0.0, 0.0),
    val acceleration: Double = 0.0,
    val random: Double = 0.0,
    val tracker: Particle? = null,
    val trackerBlock: Material? = null
)

data class DirectionalVectors(
    val vertical: Vector,
    val horizontal: Vector
)

object BallAttributesCalculator {
    fun calc(
        ballName: String,
        velocity: Vector,
        isRightHanded: Boolean,
        isFromDispenser: Boolean
    ): BallAttributes {
        val config = BallConfig.getMove(ballName)
        val moveDirection = if (isRightHanded) 1 else -1

        val velocityModifier = velocity.clone().multiply(config.velocity - 1)
        val vertical = config.vertical
        val horizontal = config.horizontal
        val directionalVectors = calcDirectionalVectors(
            velocity = velocity,
            verticalAmount = vertical,
            horizontalAmount = horizontal,
            moveDirection = moveDirection
        )
        // 回転ベクトルの計算
        val spinVector = calcSpinVector(
            velocity = velocity,
            directionalVectors = directionalVectors
        )

        // ディスペンサー補正
        if (isFromDispenser) {
            velocityModifier.add(calcDispenserCorrection(
                velocity = velocity,
                directionalVectors = directionalVectors
            ))
        }

        return BallAttributes(
            velocityModifier = velocityModifier,
            spinVector = spinVector,
            acceleration = config.acceleration,
            random = config.random,
            tracker = config.tracker,
            trackerBlock = config.trackerBlock,
        )
    }

    private fun calcSpinVector(
        velocity: Vector,
        directionalVectors: DirectionalVectors
    ): Vector {
        val (verticalMove, horizontalMove) = directionalVectors
        val moveVector = verticalMove.clone().add(horizontalMove)
        // それを実現できる回転のベクトル（球体に働くマグヌス力は球体の運動のベクトルと回転のベクトル表現の外積をなす、の逆）
        val spinVector = velocity.getCrossProduct(moveVector)
        if (spinVector.length() != 0.0) {
            // ベクトルの大きさを揃えておく
            spinVector.normalize().multiply(moveVector.length())
        }

        return spinVector
    }

    // ディスペンサーから変化球を投げるとき、ストライクゾーンを狙わせるために逆方向に補正する
    private fun calcDispenserCorrection(
        velocity: Vector,
        directionalVectors: DirectionalVectors
    ) : Vector{
        val(vertical, horizontal) = directionalVectors
        val verticalModifier: Vector = vertical.clone()
        if (vertical.getY() > 0) {
            verticalModifier.multiply(0.9)
        }
        return verticalModifier.add(horizontal.clone().multiply(0.65)).multiply(-(15 / velocity.length()))
    }

    private fun calcDirectionalVectors(
        velocity: Vector,
        verticalAmount: Double,
        horizontalAmount: Double,
        moveDirection: Int
    ): DirectionalVectors {
        // 回転ベクトルの取得処理のために、地面と水平な運動ベクトルの取得
        val linear = velocity.clone().setY(0).normalize()
        // 地面となす角度も取得
        val angle = velocity.angle(linear) * sign(velocity.getY())
        // 縦方向の変化をボールの進行方向と合わせる
        val verticalMove: Vector = Vector(linear.getX() * -sin(angle), cos(angle), linear.getZ() * -sin(angle)).normalize()
            .multiply(verticalAmount)
        // 横方向の変化も
        val horizontalMove: Vector = linear.getCrossProduct(Vector(0, 1, 0)).normalize()
            .multiply(moveDirection * horizontalAmount)

        return DirectionalVectors(verticalMove, horizontalMove)
    }
}