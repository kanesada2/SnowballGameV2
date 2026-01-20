package com.github.kanesada2.SnowballGame.entity

import com.github.kanesada2.SnowballGame.Constants
import com.github.kanesada2.SnowballGame.PersistentDataKeys
import com.github.kanesada2.SnowballGame.config.ParticleConfig
import com.github.kanesada2.SnowballGame.config.ParticleType
import com.github.kanesada2.SnowballGame.extension.BukkitRunnableExtension.later
import com.github.kanesada2.SnowballGame.extension.BukkitRunnableExtension.repeat
import com.github.kanesada2.SnowballGame.extension.MetaKeys
import com.github.kanesada2.SnowballGame.extension.MetadatableExtension.getMeta
import com.github.kanesada2.SnowballGame.extension.MetadatableExtension.hasMeta
import com.github.kanesada2.SnowballGame.extension.MetadatableExtension.removeMeta
import com.github.kanesada2.SnowballGame.extension.MetadatableExtension.setMeta
import com.github.kanesada2.SnowballGame.extension.MetadatableExtension.updateMeta
import com.github.kanesada2.SnowballGame.item.BatItem
import com.github.kanesada2.SnowballGame.task.GaugeProgressTask
import com.github.kanesada2.SnowballGame.task.PlayerCoolDownTask
import org.bukkit.*
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.entity.*
import org.bukkit.inventory.MainHand
import org.bukkit.scheduler.BukkitTask
import org.bukkit.util.Vector
import java.util.*
import kotlin.contracts.contract
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin


data class SwingAttributes(
    val center: Location,
    val hitRange: Vector,
    val force: Double,
    val rate: Double = Constants.Batter.DEFAULT_SWING_RATE,
    val batMove: Vector,
    val coefficient: Double = Constants.Batter.DEFAULT_SWING_COEFFICIENT
)
enum class SwingState {
    IDLE,
    CHARGING,
    READY
}
@JvmInline
value class Batter(override val player: Player): CanSlide {
    val batItem: BatItem
        get() = BatItem.from(player.inventory.itemInMainHand) ?:
                BatItem.from(player.inventory.itemInOffHand) ?:
                BatItem.generate()
    val isMainHanded : Boolean
        get() = BatItem.from(player.inventory.itemInMainHand) != null
    val isRight: Boolean
        get() = player.mainHand == MainHand.RIGHT && isMainHanded ||
                player.mainHand == MainHand.LEFT && !isMainHanded

    private val bossBarKey: NamespacedKey
        get() = PersistentDataKeys.publishSwingGaugeKey(player)
    val swingGauge: BossBar?
        get() = Bukkit.getBossBar(bossBarKey)
    val currentPower: Double
        get() {
            val power = (swingGauge?.progress ?: 0.0) * Constants.Batter.SWING_POWER_SNEAK_AMPLIFIER
            return if(player.isSneaking) power else power + Constants.Batter.SWING_POWER_ADJUSTMENT
        }
    val swingState: SwingState
        get() = SwingState.valueOf(player.getMeta(MetaKeys.SWING_STATE)?: SwingState.IDLE.name)
    val isIdling: Boolean
        get() = swingState == SwingState.IDLE
    val isCharging: Boolean
        get() = swingState == SwingState.CHARGING
    val isReady: Boolean
        get() = swingState == SwingState.READY

    companion object {
        private val gaugeTasks = mutableMapOf<UUID, BukkitTask>()
        fun from(player: Player): Batter? {
            val mainHand = player.inventory.itemInMainHand
            val targetInventory = if(mainHand.type != Material.AIR) mainHand else player.inventory.itemInOffHand
            BatItem.from(targetInventory)?:  return null
            return Batter(player)
        }
        fun force(player: Player): Batter {
            return Batter(player)
        }
    }

    fun swingSequence() {
        // 左右の腕による同一tick連打対策にクールタイムを設ける
        if(player.hasMeta(MetaKeys.PROHIBIT_PROGRESS_SWING)) return
        player.setMeta(MetaKeys.PROHIBIT_PROGRESS_SWING, true)
        if(isIdling){
            prepare()
        }else if(isCharging){
            stance()
        }else if(isReady){
            swing()
        }
        PlayerCoolDownTask(player).later(Constants.Batter.SWING_COOLDOWN_TICKS)
    }

    fun prepare() {
        stopSwingGauge()

        val bar = Bukkit.createBossBar(
            bossBarKey,
            "§eSwing Power",
            BarColor.YELLOW,
            BarStyle.SEGMENTED_20
        ).apply {
            progress = Constants.Batter.INITIAL_BAR_PROGRESS
            addPlayer(player)
        }
        val task = GaugeProgressTask(bar).repeat(0, 1)
        gaugeTasks[player.uniqueId] = task
        player.updateMeta(MetaKeys.SWING_STATE, SwingState.CHARGING.name)
    }

    fun stance() {
        stopSwingGauge()
        player.updateMeta(MetaKeys.SWING_STATE, SwingState.READY.name)
    }

    fun stopSwingGauge(): Double {
        val power = currentPower
        gaugeTasks.remove(player.uniqueId)?.cancel()

        return power
    }

    fun removeSwingGauge() {
        swingGauge?.let {
            it.removeAll()
            Bukkit.removeBossBar(bossBarKey)
        }
        player.removeMeta(MetaKeys.SWING_STATE)
    }

    fun swing(): List<Ball> {
        val lengthModifier = currentPower * 0.5 + 0.5 // バントのときはミートポイント近いほうがいい
        val toCenter = player.location.direction.normalize().multiply(batItem.length * lengthModifier)
        val center = player.eyeLocation.add(toCenter)
        val force = currentPower * batItem.power
        val size = batItem.range + (1 - currentPower) * Constants.Batter.HIT_RANGE_MODIFIER * batItem.range
        val range = Vector(1,1,1).multiply(size)

        val bat = Bat.spawn(center)

        if(currentPower > Constants.Batter.STRONG_SWING_THRESHOLD && ParticleConfig.isEnabled(ParticleType.SWING_BAT_SEQUENT)){
            val eye = player.eyeLocation
            var i = 0.0
            while (abs(i) < Math.PI) {
                val swing = calcBatPosition(batItem, i)
                swing.add(0.0, eye.getDirection().getY() + 1, 0.0)
                ParticleConfig.spawnIfEnabled(ParticleType.SWING_BAT_SEQUENT, swing)
                i += Constants.Batter.SWING_ARC_STEP * (if (isRight) -1 else 1)
            }
        }

        try {
            return bat.swing(
                player = player,
                batItem = batItem.item,
                force = force,
                range = range,
                batMove = calcBatMove(batItem),
            )
        } finally {
            bat.remove()
            removeSwingGauge()
        }
    }

    private fun calcBatMove(bat: BatItem): Vector {
        val rollDirection = if (isRight) -1 else 1
        // へその前（後頭部から90度回ったところ）から少し進めたところから少し押せるのがジャストタイミングでのインパクト
        return calcBatPosition(bat, (Math.PI / 2 + Constants.Batter.IMPACT_ADJUSTMENT) * rollDirection)
            .subtract(calcBatPosition(bat, Math.PI / 2 * rollDirection))
            .toVector().normalize()
    }

    private fun calcBatPosition(bat: BatItem, roll: Double): Location {
        val eyeLoc = player.eyeLocation
        val rollDirection = if (isRight) -1 else 1

        eyeLoc.yaw -= (90 * rollDirection - Math.toDegrees(roll)).toFloat()

        val push = eyeLoc.direction.setY(0).normalize()
        val theta = abs(roll * 2) + Math.PI * bat.fly

        return player.eyeLocation.clone().add(
            push.x * (theta - sin(theta)),
            -(1 - cos(theta)),
            push.z * (theta - sin(theta))
        )
    }
}