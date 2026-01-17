package com.github.kanesada2.SnowballGame.entity

import com.github.kanesada2.SnowballGame.PersistentDataKeys
import com.github.kanesada2.SnowballGame.api.BallBounceEvent
import com.github.kanesada2.SnowballGame.api.BallThrownEvent
import com.github.kanesada2.SnowballGame.config.BallConfig
import com.github.kanesada2.SnowballGame.config.BounceConfig
import com.github.kanesada2.SnowballGame.config.ParticleConfig
import com.github.kanesada2.SnowballGame.config.ParticleType
import com.github.kanesada2.SnowballGame.extension.BukkitRunnableExtension.repeat
import com.github.kanesada2.SnowballGame.extension.MetaKeys
import com.github.kanesada2.SnowballGame.extension.MetadatableExtension.getMeta
import com.github.kanesada2.SnowballGame.extension.MetadatableExtension.setMeta
import com.github.kanesada2.SnowballGame.extension.getPdc
import com.github.kanesada2.SnowballGame.extension.hasPdc
import com.github.kanesada2.SnowballGame.extension.knockedBackedByProjectile
import com.github.kanesada2.SnowballGame.extension.setPdc
import com.github.kanesada2.SnowballGame.item.BallItem
import com.github.kanesada2.SnowballGame.service.BallBounceCalculator
import com.github.kanesada2.SnowballGame.service.BallHitFaceResolver
import com.github.kanesada2.SnowballGame.service.BallHitLocationCorrector
import com.github.kanesada2.SnowballGame.task.BallMovingTask
import com.github.kanesada2.SnowballGame.task.BallRollingTask
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.projectiles.ProjectileSource
import org.bukkit.util.Vector


data class LaunchSettings(
    val shooter: ProjectileSource,
    val velocity: Vector,
    val rPoint: Location,
    val ballType: String,
    // 以下はデフォルト値あり
    val hand: ItemStack? = null,
    val isPitching: Boolean = false,
    val ballName: String = "Ball",
    val spinVector: Vector = Vector(0, 0, 0),
    val acceleration: Double = 0.0,
    val random: Double = 0.0,
    val tracker: Particle? = null,
    val trackerBlock: Material? = null,
    val velocityModifier: Vector = Vector(0, 0, 0)
)

data class BounceSettings(
    val hitBlock: Block,
    val repulsion: Vector = Vector(0.7, 0.4, 0.7) // という設定
)

@JvmInline
value class Ball(val projectile : Projectile) {
    var isInFlight: Boolean
        get() = projectile.getMeta(MetaKeys.IS_IN_FLIGHT)?:false
        set(value) = projectile.setMeta(MetaKeys.IS_IN_FLIGHT, value)
    var spinVector: Vector
        get() = projectile.getMeta(MetaKeys.SPIN_VECTOR)?: Vector(0, 0, 0)
        set(value) = projectile.setMeta(MetaKeys.SPIN_VECTOR, value)
    var movingType: String
        get() = projectile.getMeta(MetaKeys.MOVING_TYPE)?: "Ball"
        set(value) = projectile.setMeta(MetaKeys.MOVING_TYPE, value)
    val prevBoundLocation: Location?
        get() = projectile.getMeta(MetaKeys.PREV_BOUND_LOCATION)
    val ballType: String
        get() = projectile.getPdc(PersistentDataKeys.BallType, PersistentDataType.STRING)?: "Normal"
    val reusable: Boolean
        get() = BallConfig.getMove(movingType).reusable
    val nameForDrop: String
        get() = if(reusable) movingType else BallConfig.defaultName
    companion object {
        fun launch(setting: LaunchSettings) : Ball{
            val (shooter, velocity, rPoint, ballType, hand, isPitching, ballName, spinVector, acceleration, random, tracker, trackerBlock, velocityModifier) = setting

            // ボールの初期化
            val launched = rPoint.getWorld()!!.spawnEntity(rPoint, EntityType.SNOWBALL) as Projectile
            launched.setGravity(true)
            launched.isGlowing = true
            launched.velocity = velocity.add(velocityModifier)
            launched.shooter = shooter
            launched.setMeta(MetaKeys.MOVING_TYPE, ballName)
            launched.setPdc(PersistentDataKeys.BallType, PersistentDataType.STRING, ballType)
            (shooter as? Player)?.let{player->
                if (player.gameMode != GameMode.CREATIVE) {
                    hand?.let { it.amount -= 1 }
                }
            }
            if (isPitching) {
                Bukkit.getPluginManager().callEvent(BallThrownEvent(launched))
                val entities: MutableCollection<Entity> = launched.getNearbyEntities(50.0, 10.0, 50.0)
                for (entity in entities) {
                    if (entity is ArmorStand) {
                        Umpire.from(entity)?.prepare(launched)
                    }
                }
            }
            if (!(spinVector.length() == 0.0 && acceleration == 0.0 && random == 0.0 && tracker == null)) {
                BallMovingTask(launched, spinVector, acceleration, tracker, trackerBlock, random).repeat(0, 1)
            }
            return Ball(launched)
        }
        fun from(projectile: Projectile): Ball? {
            if(!BallConfig.enabled) return null
            return if(projectile.hasPdc(PersistentDataKeys.BallType)) Ball(projectile) else null
        }
    }

    fun bounce(settings: BounceSettings): Projectile {
        val (hitBlock, repulsion) = settings

        if (repulsion.x < 0 || repulsion.y < 0 || repulsion.z < 0) {
            Bukkit.getLogger().info("Repulsion rate must be positive.")
            return projectile
        }

        // 同じ場所でのバウンド回数チェック
        val samePlace = checkSamePlaceBounce()

        // 通り抜け判定
        if (BounceConfig.isPassthrough(hitBlock) || samePlace > 5) {
            return launchThrough()
        }

        // 衝突面を特定
        val hitFace = BallHitFaceResolver.resolve(projectile, hitBlock)

        // 物理計算
        val result = BallBounceCalculator.calculateBounce(
            velocity = projectile.velocity,
            hitFace = hitFace,
            repulsion = repulsion.multiply(BounceConfig.getRepulsion(hitBlock)),
            spin = spinVector
        )

        // projectileHitがなぜかめり込んだ位置で衝突したことにしてくることがあるので、対策
        val hitLocation = BallHitLocationCorrector.correctHitLocationIfNeeded(projectile.location, hitFace, hitBlock)

        if(result.shouldDrop){
            dropAsItem()
            return projectile
        }
        // 転がりに移行
        if (result.shouldRoll && BounceConfig.isPassthrough(hitBlock.getRelative(BlockFace.UP))) {
            return roll(hitBlock, hitLocation)
        }
        // 新しいボールを生成
        return launchBounced(result, hitLocation, samePlace, hitBlock)
    }

    private fun checkSamePlaceBounce(): Int {
        val hitLoc = projectile.location
        return prevBoundLocation?.let {
            if (hitLoc.distanceSquared(it) < 1.0) {
                (projectile.getMeta(MetaKeys.SAME_PLACE_COUNT) ?: 0) + 1
            } else 0
        } ?: 0
    }


    private fun launchThrough(): Projectile {
        val hitLoc = projectile.location.add(projectile.velocity).apply { y += 0.1 }
        return launch(LaunchSettings(
            shooter = projectile.shooter!!,
            ballType = this.ballType,
            velocity = projectile.velocity,
            rPoint = hitLoc,
            ballName = nameForDrop
        )).projectile
    }

    private fun launchBounced(result: BallBounceCalculator.BounceResult, hitLocation: Location,  samePlace: Int, hitBlock: Block): Projectile {
        val bounced = launch(LaunchSettings(
            shooter = projectile.shooter!!,
            ballType = this.ballType,
            velocity = result.velocity,
            rPoint = hitLocation,
            ballName = nameForDrop
        )).projectile

        bounced.setMeta(MetaKeys.SPIN_VECTOR, result.spin)
        bounced.setMeta(MetaKeys.PREV_BOUND_LOCATION, projectile.location)
        bounced.setMeta(MetaKeys.SAME_PLACE_COUNT, samePlace)

        Bukkit.getPluginManager().callEvent(
            BallBounceEvent(bounced, hitBlock, projectile, this.isInFlight)
        )

        return bounced
    }


    fun roll(hitBlock: Block, hitLocation: Location) : Projectile{
        val velocity = projectile.velocity
        velocity.y = 0.0
        // 薄いブロックへの衝突の場合、そのブロックを避けられるくらいにずらしてあげる
        if (BounceConfig.isAlwaysTop(hitBlock)) {
            hitLocation.y += 0.15
        } else {
            hitLocation.y = hitLocation.blockY.toDouble()
        }
        val bounced = launch(LaunchSettings(
            shooter = projectile.shooter!!,
            ballType = this.ballType,
            velocity = velocity,
            rPoint = hitLocation,
            ballName = nameForDrop
        )).projectile
        bounced.setGravity(false)
        BallRollingTask(bounced).repeat(0,1)
        val bounceEvent = BallBounceEvent(bounced, hitBlock, projectile, this.isInFlight)
        Bukkit.getPluginManager().callEvent(bounceEvent)
        return bounced
    }

    fun dropAsItem() : BallItem {
        val item = BallItem.generate(ballType, nameForDrop)
        projectile.world.dropItem(projectile.location, item.item)
        return item
    }

    fun hitToEntity(entity: Entity) {
        (entity as? ArmorStand)?.let(Coach::from)?.let {
            it.handleBall(this)
            return
        }
        (entity as? Player)?.let{player ->
            Fielder.from(player)?.let {
                it.catch(this)
                return
            }
            // グラブを持ってないプレイヤーはノックバックする
            player.knockedBackedByProjectile(projectile)
            ParticleConfig.spawnIfEnabled(ParticleType.HIT_TO_PLAYERS, player.location)
        }
        dropAsItem()
    }

}