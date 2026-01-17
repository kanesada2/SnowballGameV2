package com.github.kanesada2.SnowballGame.service

import org.bukkit.block.BlockFace
import org.bukkit.util.Vector
import kotlin.math.abs
import kotlin.math.pow

object BallBounceCalculator {

    data class BounceResult(
        val velocity: Vector,
        val spin: Vector,
        val shouldRoll: Boolean,
        val shouldDrop: Boolean = false
    )

    fun calculateBounce(
        velocity: Vector,
        hitFace: BlockFace,
        repulsion: Vector,
        spin: Vector
    ): BounceResult {
        // 失速していたらアイテム化
        if(velocity.length() < 0.15){
            return BounceResult(velocity, spin, shouldRoll = false, shouldDrop = true)
        }
        // 転がり判定
        if (hitFace == BlockFace.UP && abs(velocity.y) < 0.15) {
            return BounceResult(velocity.clone().setY(0), spin, shouldRoll = true)
        }

        val reflected = reflect(velocity, hitFace)
        val spinEffect = calculateSpinEffect(velocity, spin, repulsion)
        val angle = calculateImpactAngle(velocity, hitFace)

        val result = applyRepulsion(reflected, repulsion, angle)
            .apply { multiply(1.3.pow(-length())) }
            .add(spinEffect)

        val newSpin = calculateNewSpin(spin, velocity, hitFace)

        return BounceResult(result, newSpin, shouldRoll = false)
    }

    private fun reflect(velocity: Vector, hitFace: BlockFace): Vector {
        val v = velocity.clone()
        when (hitFace) {
            BlockFace.SOUTH, BlockFace.NORTH -> v.z = -v.z
            BlockFace.EAST, BlockFace.WEST -> v.x = -v.x
            else -> v.y = -v.y
        }
        return v
    }

    private fun calculateSpinEffect(velocity: Vector, spin: Vector, repulsion: Vector): Vector {
        if (spin.length() == 0.0 || velocity.length() == 0.0) {
            return Vector(0, 0, 0)
        }

        return spin.clone()
            .multiply(-1)
            .getCrossProduct(velocity)
            .normalize()
            .multiply(spin.length() * 9)
            .apply {
                x *= repulsion.x
                y *= repulsion.y
                z *= repulsion.z
            }
    }

    private fun calculateImpactAngle(velocity: Vector, hitFace: BlockFace): Double {
        val linear = when (hitFace) {
            BlockFace.SOUTH, BlockFace.NORTH -> velocity.clone().setZ(0)
            BlockFace.EAST, BlockFace.WEST -> velocity.clone().setX(0)
            else -> velocity.clone().setY(0)
        }

        val angle = velocity.angle(linear).toDouble()
        return if (angle.isNaN()) 0.0 else angle / Math.toRadians(90.0)
    }

    private fun applyRepulsion(velocity: Vector, repulsion: Vector, angle: Double): Vector {
        return velocity.apply {
            x *= repulsion.x.pow(angle)
            y *= repulsion.y.pow(angle)
            z *= repulsion.z.pow(angle)
        }
    }

    private fun calculateNewSpin(spin: Vector, velocity: Vector, hitFace: BlockFace): Vector {
        val linear = when (hitFace) {
            BlockFace.SOUTH, BlockFace.NORTH -> velocity.clone().setZ(0)
            BlockFace.EAST, BlockFace.WEST -> velocity.clone().setX(0)
            else -> velocity.clone().setY(0)
        }
        val normal = if (linear != velocity) {
            linear.clone().subtract(velocity).normalize()
        } else Vector(0, 0, 0)

        return spin.clone()
            .multiply(0.01)
            .add(linear.getCrossProduct(normal).multiply(0.003))
    }
}