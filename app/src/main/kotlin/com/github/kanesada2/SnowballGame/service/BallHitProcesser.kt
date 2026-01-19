package com.github.kanesada2.SnowballGame.service

import com.github.kanesada2.SnowballGame.Constants
import com.github.kanesada2.SnowballGame.api.BallHitEvent
import com.github.kanesada2.SnowballGame.api.PlayerHitBallEvent
import com.github.kanesada2.SnowballGame.config.BallConfig
import com.github.kanesada2.SnowballGame.config.ParticleConfig
import com.github.kanesada2.SnowballGame.config.ParticleType
import com.github.kanesada2.SnowballGame.entity.Ball
import com.github.kanesada2.SnowballGame.entity.LaunchSettings
import com.github.kanesada2.SnowballGame.entity.SwingAttributes
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import kotlin.math.pow

object BallHitProcessor {

    fun processHit(
        ball: Ball,
        attributes: SwingAttributes,
        player: Player
    ): Ball {
        val projectile = ball.projectile
        val fromCenter = projectile.location.toVector().subtract(attributes.center.toVector())

        val adjustedCoefficient = attributes.coefficient * BallConfig.getCoefficient(ball.ballType)
        val power = attributes.force * attributes.rate.pow(-fromCenter.length())

        val velocity = calculateHitVelocity(
            originalVelocity = projectile.velocity,
            batMove = attributes.batMove,
            fromCenter = fromCenter,
            power = power,
            coefficient = adjustedCoefficient
        )

        val spinVector = calculateSpinVector(fromCenter, velocity, attributes.batMove, attributes.force)

        projectile.remove()

        return launchHitBall(player, ball, velocity, spinVector)
    }

    private fun calculateHitVelocity(
        originalVelocity: Vector,
        batMove: Vector,
        fromCenter: Vector,
        power: Double,
        coefficient: Double
    ): Vector {
        return originalVelocity.clone()
            .multiply(Constants.HitProcessing.ORIGINAL_VELOCITY_DAMPING)
            .add(batMove.clone().add(fromCenter.clone().normalize().multiply(Constants.HitProcessing.HIT_DIRECTION_MULTIPLIER)))
            .multiply(power * coefficient)
    }

    private fun calculateSpinVector(
        fromCenter: Vector,
        hitVelocity: Vector,
        batMove: Vector,
        force: Double
    ) : Vector {
        // スケールが違うので正規化
        val batDirection = batMove.clone().normalize()
        val hitDirection = hitVelocity.clone().normalize()

        // 打球方向とバットの動きの差分
        val relativeDirection = batDirection.subtract(hitDirection)

        // 打球方向と平行な成分
        val normalComponent = hitDirection.multiply(
            relativeDirection.dot(hitDirection)
        )
        // 打球速度と垂直な成分（平行な成分以外）
        val tangentialDirection = relativeDirection.subtract(normalComponent)

        // 打球の水平方向ベクトル（Y=0にした方向）
        val hitHorizontal = Vector(hitDirection.x, 0.0, hitDirection.z)
        if (hitHorizontal.lengthSquared() < 0.0001) {
            // 真上/真下への打球の場合
            return Vector(0, 0, 0)
        }
        hitHorizontal.normalize()

        val upward = Vector(0, 1, 0)
        val rightward = hitHorizontal.getCrossProduct(upward)

        // 接線ベクトルの大きさを上下と左右に分解
        val verticalSpin = tangentialDirection.dot(upward)  // 上下方向の擦れ
        val horizontalSpin = tangentialDirection.dot(rightward)  // 左右方向の擦れ

        // バックスピン成分：打球の進行方向に垂直な水平軸周りの回転
        val backspinVector = rightward.multiply(verticalSpin).multiply(Constants.HitProcessing.BACKSPIN_MULTIPLIER) // バットの形状的にバックスピン成分のほうがずっと強くなるはず

        // サイドスピン成分：鉛直軸周りの回転
        val sidespinVector = upward.multiply(-horizontalSpin)

        // 合成
        val totalSpin = backspinVector.add(sidespinVector)

        // 芯を外していて、強くスイングしているほど回転がかかる
        val coefficient = fromCenter.length() * Constants.HitProcessing.OFF_CENTER_SPIN_COEFFICIENT * force

        return  totalSpin.normalize().multiply(coefficient)
    }

    private fun launchHitBall(
        player: Player,
        originalBall: Ball,
        velocity: Vector,
        spinVector: Vector
    ): Ball {
        val hitEvent = PlayerHitBallEvent(
            player,
            originalBall.projectile,
            spinVector,
            velocity,
            0.0,
            0.0,
            tracker = if(ParticleConfig.isEnabled(ParticleType.BATTED_BALL_IN_FLIGHT))
                        ParticleConfig[ParticleType.BATTED_BALL_IN_FLIGHT].particle else null,
            trackerBlock = if(ParticleConfig.isEnabled(ParticleType.BATTED_BALL_IN_FLIGHT))
                            ParticleConfig[ParticleType.BATTED_BALL_IN_FLIGHT].block else null,
        )
        Bukkit.getPluginManager().callEvent(hitEvent)

        val hitBall = Ball.launch(LaunchSettings(
            shooter = hitEvent.player,
            velocity = hitEvent.velocity,
            ballType = originalBall.ballType,
            rPoint = originalBall.projectile.location,
            spinVector = hitEvent.spinVector,
            acceleration = hitEvent.acceleration,
            random = hitEvent.random,
            tracker = hitEvent.tracker,
            trackerBlock = hitEvent.trackerBlock,
        ))
        hitBall.isInFlight = true

        Bukkit.getPluginManager().callEvent(
            BallHitEvent(hitBall.projectile, originalBall.projectile)
        )

        return hitBall
    }
}