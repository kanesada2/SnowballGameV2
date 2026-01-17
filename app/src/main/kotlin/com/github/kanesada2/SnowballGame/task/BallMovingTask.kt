package com.github.kanesada2.SnowballGame.task

import com.github.kanesada2.SnowballGame.config.ParticleConfig
import com.github.kanesada2.SnowballGame.config.ParticleType
import com.github.kanesada2.SnowballGame.extension.MetaKeys
import com.github.kanesada2.SnowballGame.extension.MetadatableExtension.setMeta
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Projectile
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import kotlin.math.sin


class BallMovingTask : BukkitRunnable {
    private var actualMove: Vector = Vector(0, 0, 0)
    private var spinVector: Vector = Vector(0, 0, 0)
    private val ball: Projectile
    private var particle: Particle? = null
    private var particleBlock: Material? = null
    private var random = 0.0
    private var acceleration = 0.0
    private var randomX = 0.0
    private var randomY = 0.0
    private var randomZ = 0.0

    constructor(ball: Projectile, spinVector: Vector, acceleration: Double, particle: Particle?, particleBlock: Material?, random: Double) {
        this.ball = ball
        this.spinVector = spinVector
        if (acceleration != 0.0) {
            this.acceleration = acceleration
        }
        this.particle = particle
        this.particleBlock = particleBlock
        this.random = random
        if (random != 0.0) {
            this.randomX = Math.random() * 2 * Math.PI
            this.randomY = Math.random() * 2 * Math.PI
            this.randomZ = Math.random() * 2 * Math.PI
        }
        ball.setMeta(MetaKeys.SPIN_VECTOR, spinVector)
    }

    override fun run() {
        if (ball.isDead) {
            this.cancel()
        }
        val velocity: Vector = ball.velocity
        if (spinVector.length() != 0.0 || acceleration != 0.0) {
            // 運動のベクトルと回転のベクトル表示の外積がマグヌス力
            actualMove = spinVector.getCrossProduct(velocity)
            // 空気抵抗を考慮し、毎tick回転数を1％減衰する
            this.spinVector.multiply(0.99)
            if (actualMove.length() != 0.0) {
                actualMove.normalize().multiply(spinVector.length())
            }
            if(velocity.length() != 0.0) {
                actualMove.add(velocity.clone().normalize().multiply(acceleration))
            }
            velocity.add(actualMove)
        }
        if (random != 0.0) {
            this.randomX += Math.random() * 0.3
            this.randomY += Math.random() * 0.3
            this.randomZ += Math.random() * 0.3
            // 連続して同じ方向への変化がある程度出やすいよう、正弦曲線上をランダムな始点から十分に小さいランダムなステップで進む
            val toAdd = Vector(sin(randomX), sin(randomY), sin(randomZ))
            toAdd.multiply(random)
            velocity.add(toAdd)
        }
        ball.velocity = velocity
        if(!ParticleConfig.isEnabled(ParticleType.MOVING_BALL)) return
        particle?.let { particle->
            if (particle.dataType == BlockData::class.java) {
                particleBlock?.let { block ->
                    ball.world.spawnParticle(particle, ball.location, 5, 0.5, 0.5, 0.5, Bukkit.createBlockData(block))
                    return
                }
            }
            ball.world.spawnParticle(particle, ball.location, 5, 0.5, 0.5, 0.5)
        }
    }
}