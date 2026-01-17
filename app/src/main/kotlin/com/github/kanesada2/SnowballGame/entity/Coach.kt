package com.github.kanesada2.SnowballGame.entity

import com.github.kanesada2.SnowballGame.PersistentDataKeys
import com.github.kanesada2.SnowballGame.config.*
import com.github.kanesada2.SnowballGame.extension.hasPdc
import com.github.kanesada2.SnowballGame.extension.setPdc
import com.github.kanesada2.SnowballGame.item.BallItem
import com.github.kanesada2.SnowballGame.item.BatItem
import com.github.kanesada2.SnowballGame.item.CoachItem
import com.github.kanesada2.SnowballGame.item.GloveItem
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.*
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.projectiles.BlockProjectileSource
import org.bukkit.projectiles.ProjectileSource
import org.bukkit.util.Vector
import kotlin.math.pow
import kotlin.math.sign
import kotlin.math.sqrt


sealed interface CoachRole {
    fun handleBall(ball: Ball)
}

@JvmInline
value class Coach(val stand: ArmorStand) {
    val heldItem: ItemStack
        get() = stand.equipment?.itemInMainHand ?: stand.equipment?.itemInOffHand ?: ItemStack(Material.AIR)
    val defaultEquipments: Array<ItemStack>
        get() = arrayOf(
            ItemStack(Material.LEATHER_BOOTS),
            ItemStack(Material.LEATHER_LEGGINGS),
            ItemStack(Material.LEATHER_CHESTPLATE),
            ItemStack(Material.CREEPER_HEAD),
            BatItem.generate().item
        )
    companion object {
        fun from(stand: ArmorStand): Coach?{
            if(!CoachConfig.enabled) return null
            if(!stand.hasPdc(PersistentDataKeys.Coach)) return null
            return Coach(stand)
        }
        fun spawn(player: Player, location: Location): Coach? {
            val targetLocation = location.add(0.5, 0.0, 0.5)
            val direction = player.location.subtract(targetLocation).toVector()
            targetLocation.setDirection(direction)
            (location.world?.spawnEntity(targetLocation, EntityType.ARMOR_STAND) as? ArmorStand)?.apply {
                setPdc(PersistentDataKeys.Coach, PersistentDataType.BOOLEAN, true)
                isCustomNameVisible = true
                customName = CoachConfig.name
                setArms(true)
                isGlowing = true
                val coach = from(this)
                coach?.let { equipment?.armorContents = it.defaultEquipments }
                equipment?.setItemInMainHand(BatItem.generate().item)
                return coach
            }
            return null
        }
    }
    fun currentRole(): CoachRole {
        return Knocker.from(this)
            ?: Target.from(this)
            ?: Pivot.mark(this) // 条件に合う持ち物がないときはピボットマンとなる
    }
    fun handleBall(ball: Ball) {
        currentRole().handleBall(ball)
    }

    fun remove(){
        stand.remove()
        val drops = stand.equipment?.armorContents?.toMutableList()?: return
        drops.add(stand.equipment?.itemInMainHand)
        drops.add(stand.equipment?.itemInOffHand)
        drops.removeAll(defaultEquipments.toSet())
        drops.add(CoachItem.generate(stand.customName).item)
        for (drop in drops) {
            if (drop.type == Material.AIR) {
                continue
            }
            stand.world.dropItem(stand.location, drop)
        }
    }

    fun changeCloth(item : ItemStack, slot: EquipmentSlot): ItemStack?{
        val equipped = stand.equipment?.getItem(slot)
        stand.equipment?.setItem(slot, item)
        if(defaultEquipments.contains(equipped)) return ItemStack(Material.AIR)
        return equipped
    }
}

@JvmInline
value class Knocker(val coach: Coach): CoachRole{
    companion object{
        fun from(coach: Coach) : Knocker? {
            BatItem.from(coach.heldItem)?: return null
            return Knocker(coach)
        }
    }

    override fun handleBall(ball: Ball) {
        var actualPlayer: Player
        // ディスペンサーからボールをぶつけられた場合は近くにいるランダムな野手にノックを打つ
        if (ball.projectile.shooter is BlockProjectileSource) {
            val loc: Location = coach.stand.location
            val players: List<Player> = loc.world?.players ?: emptyList()
            var range = CoachConfig.range
            range *= range
            val fielders: MutableList<Player> = emptyList<Player>().toMutableList()
            for (player in players) {
                if (loc.distanceSquared(player.location) <= range && Fielder.from(player) != null) {
                    fielders.add(player)
                }
            }
            if (fielders.isEmpty()) return
            actualPlayer = fielders.random()
        }else {
            actualPlayer = ball.projectile.shooter as? Player?: return
        }
        hitBall(ball.ballType, actualPlayer)
    }

    fun hitBall(ballType: String, player: Player) : Projectile{
        val knockedVec = player.location.toVector().subtract(coach.stand.location.toVector()).normalize()
        val distance = sqrt(player.location.distanceSquared(coach.stand.location))

        // 重いシミュレートを伴う飛距離計算抜きにちょうどいいくらいの打球にするために、ここからひたすらに秘伝のタレ
        // コクのある乱数（自然分布に近い）に距離に比例する数をかけることで、極端なフライやゴロが出にくいように
        val randomY = (Math.random() - Math.random()) * (distance / 30)
        // ゴロなら強く、フライなら弱めに打ったほうがいいので
        knockedVec.multiply(2.2.pow(-randomY))

        val randomizer = Vector(
            (Math.random() - Math.random()) / (distance / 8),
            randomY,
            (Math.random() - Math.random()) / (distance / 8)
        )
        knockedVec.add(randomizer)
        // 極端なフライやゴロは更に少し弱めておく
        if (knockedVec.angle(knockedVec.clone().setY(0)) > 0.5) {
            knockedVec.multiply(0.7)
        }
        val angle = knockedVec.angle(knockedVec.clone().setY(0)) * sign(knockedVec.getY())
        val coefficient =BallConfig.getCoefficient(ballType)

        if (angle > 30) {
            knockedVec.setY(knockedVec.getY() * coefficient.pow(2.0))
        } else {
            knockedVec.setY(knockedVec.getY() / coefficient.pow(2.0))
        }
        // 打球の飛距離を最後に調整
        knockedVec.multiply(coefficient * distance / 25)
        val spinVector = Vector.getRandom().normalize().multiply(0.005 / (1 + 2.0.pow(-knockedVec.length())))

        player.sendMessage("Catch the ball!!!")
        val newBall = Ball.launch(LaunchSettings(
            shooter = coach.stand as ProjectileSource,
            velocity = knockedVec,
            spinVector = spinVector,
            tracker = if(ParticleConfig.isEnabled(ParticleType.BATTED_BALL_IN_FLIGHT))
                ParticleConfig[ParticleType.BATTED_BALL_IN_FLIGHT].particle else null,
            trackerBlock = if(ParticleConfig.isEnabled(ParticleType.BATTED_BALL_IN_FLIGHT))
                ParticleConfig[ParticleType.BATTED_BALL_IN_FLIGHT].block else null,
            rPoint = coach.stand.eyeLocation.add(knockedVec.clone().normalize().multiply(0.5)),
            ballType = ballType,
        ))
        newBall.isInFlight = true
        return newBall.projectile
    }
}

@JvmInline
value class Pivot(val coach: Coach): CoachRole{
    companion object{
        fun from(coach: Coach) : Pivot? {
            BallItem.from(coach.heldItem)?: return null
            return Pivot(coach)
        }
        // フォールバック用
        fun mark(coach: Coach): Pivot {
            return Pivot(coach)
        }
    }

    override fun handleBall(ball: Ball) {
        // ピボットマン同士で無限キャッチボールができないようにする
        (ball.projectile.shooter as? ArmorStand)
            ?.let{ Coach.from(it)}
            ?.let{if(from(it) != null) return}
        val velocity: Vector = coach.stand.eyeLocation.getDirection().normalize().multiply(1.5)
        Ball.launch(LaunchSettings(
            shooter = coach.stand as ProjectileSource,
            velocity = velocity,
            rPoint = coach.stand.eyeLocation,
            ballType = ball.ballType
        ))
    }
}

@JvmInline
value class Target(val coach: Coach): CoachRole{
    companion object{
        fun from(coach: Coach) : Target? {
            GloveItem.from(coach.heldItem)?: return null
            return Target(coach)
        }
    }

    override fun handleBall(ball: Ball) {
        var location: Location = coach.stand.location
        MessageConfig.broadcast(MessageType.CATCH, location, "PLAYER" to (coach.stand.customName ?: CoachConfig.name))
        val itemBall = BallItem.generate(ball.ballType)

        if (ball.projectile.shooter is LivingEntity) {
            location = (ball.projectile.shooter as LivingEntity).eyeLocation
        } else if (ball.projectile.shooter is BlockProjectileSource) {
            val source = ball.projectile.shooter as BlockProjectileSource
            location = source.block.location.add(0.5, 2.2, 0.5)
        }
        ball.projectile.world.dropItem(location, itemBall.item)
    }
}