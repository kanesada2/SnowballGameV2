package com.github.kanesada2.SnowballGame.api

import com.github.kanesada2.SnowballGame.config.BatConfig
import com.github.kanesada2.SnowballGame.entity.Ball
import com.github.kanesada2.SnowballGame.entity.Ballmaker
import com.github.kanesada2.SnowballGame.entity.Base
import com.github.kanesada2.SnowballGame.entity.Bat
import com.github.kanesada2.SnowballGame.entity.BounceSettings
import com.github.kanesada2.SnowballGame.entity.Coach
import com.github.kanesada2.SnowballGame.entity.Fielder
import com.github.kanesada2.SnowballGame.entity.Knocker
import com.github.kanesada2.SnowballGame.entity.LaunchSettings
import com.github.kanesada2.SnowballGame.entity.Umpire
import com.github.kanesada2.SnowballGame.item.BallItem
import com.github.kanesada2.SnowballGame.item.BaseItem
import com.github.kanesada2.SnowballGame.item.BatItem
import com.github.kanesada2.SnowballGame.item.CoachItem
import com.github.kanesada2.SnowballGame.item.GloveItem
import com.github.kanesada2.SnowballGame.item.UmpireItem
import com.github.kanesada2.SnowballGame.service.BallAttributes
import com.github.kanesada2.SnowballGame.service.BallAttributesCalculator
import com.github.kanesada2.SnowballGame.service.GloveAttributes
import com.github.kanesada2.SnowballGame.service.GloveAttributesCalculator
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Item
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.entity.Snowman
import org.bukkit.inventory.ItemStack
import org.bukkit.projectiles.ProjectileSource
import org.bukkit.util.Vector
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin


/**
 * Core API class for SnowballGame plugin providing baseball game mechanics.
 *
 * This class contains static methods for:
 * - Launching balls with custom physics
 * - Detecting and handling bat-ball collisions
 * - Catching mechanics
 * - Coach training interactions
 * - Ball bounce physics
 * - Position and modifier calculations
 *
 */
object SnowballGameAPI {

    /**
     * Launches a projectile (ball) with custom physics properties.
     *
     * @param shooter The entity shooting the ball
     * @param hand The item in hand (ball item)
     * @param isPitching Whether this is a pitching action
     * @param ballType The type of ball (highest, higher, lower, lowest)
     * @param ballName The display name of the ball
     * @param velocity Initial velocity vector
     * @param spinVector Spin applied to the ball
     * @param acceleration Acceleration value
     * @param random Randomness factor
     * @param tracker Particle effect for tracking
     * @param rPoint Release point location
     * @param vModifier Velocity modifier vector
     * @return The launched projectile
     */
    @JvmStatic
    fun launch(
        shooter: ProjectileSource,
        hand: ItemStack?,
        isPitching: Boolean,
        ballType: String,
        ballName: String,
        velocity: Vector,
        spinVector: Vector,
        acceleration: Double,
        random: Double,
        tracker: Particle?,
        rPoint: Location,
        vModifier: Vector
    ): Projectile {
       return Ball.launch(LaunchSettings(
           shooter = shooter,
           hand = hand,
           isPitching = isPitching,
           ballType = ballType,
           ballName = ballName,
           velocity = velocity,
           spinVector = spinVector,
           acceleration = acceleration,
           random = random,
           tracker = tracker,
           rPoint = rPoint,
           velocityModifier = vModifier
       )).projectile
    }

    /**
     * Attempts to hit a ball with a bat within the specified range.
     *
     * @param player The player swinging the bat
     * @param center The center location of the swing
     * @param hitRange The hit detection range
     * @param force The force of the swing
     * @param rate Rate parameter for power calculation
     * @param batMove Bat movement vector
     * @param coefficient Physics coefficient
     * @return The hit ball projectile, or null if no ball was hit
     */
    @JvmStatic
    fun tryHit(
        player: Player,
        center: Location,
        hitRange: Vector,
        force: Float, // 歴史的理由（後方互換のためそのまま）
        rate: Double,
        batMove: Vector,
        coefficient: Double
    ): Projectile? {
        val bat = Bat.spawn(center)
        try {
            val balls = bat.swing(
                player = player,
                range = hitRange,
                force = force.toDouble(),
                rate = rate,
                batMove = batMove,
                coefficient = coefficient
            )
            if(balls.isEmpty()) return null
            return balls.first().projectile}
        finally {
            bat.remove()
        }
    }

    /**
     * Attempts to catch a ball within the specified range.
     *
     * @param player The player attempting to catch
     * @param from The location to check from
     * @param range The catch range vector
     * @param rate Success rate multiplier
     * @return true if a ball was caught or attempted, false otherwise
     */
    @JvmStatic
    fun tryCatch(
        player: Player,
        from: Location,
        range: Vector,
        rate: Double
    ): Boolean {
        return Fielder.mark(player).tryCatch(from, range, rate)
    }

    /**
     * Makes a coach entity throw a ball to a player for practice.
     *
     * @param player The target player
     * @param coach The coach entity
     * @param ballType The type of ball to throw
     * @return The thrown projectile
     */
    @JvmStatic
    fun playWithCoach(
        player: Player,
        coach: LivingEntity,
        ballType: String
    ): Projectile? {
        val tmpCoach = Coach.spawn(player, coach.location)
        try {
            val knocker = tmpCoach?.let{Knocker.from(tmpCoach)} ?: return null
            return knocker.hitBall(ballType, player)
        }finally {
            tmpCoach?.remove()
        }
    }

    /**
     * Handles ball bounce physics when hitting a block.
     *
     * @param ball The ball that bounced
     * @param hitBlock The block that was hit
     * @param repulsion Repulsion rate vector (X, Y, Z components)
     * @param spinVector Current spin vector
     * @param isFirst Whether this is the first bounce
     * @return The bounced projectile
     */
    @JvmStatic
    fun bounce(
        ball: Projectile,
        hitBlock: Block,
        repulsion: Vector,
        spinVector: Vector,
        isFirst: Boolean
    ): Projectile {
        val bounced = Ball.from(ball)?: return ball
        bounced.spinVector = spinVector
        bounced.isInFlight = isFirst
        return bounced.bounce(BounceSettings(
            hitBlock = hitBlock,
            repulsion = repulsion,
        ))
    }

    /**
     * Calculates bat position based on player eye location and swing parameters.
     *
     * @param eye Player eye location
     * @param roll Roll angle in radians
     * @param rollDirection Roll direction (1 or -1)
     * @param batName Bat swing type name
     * @return Calculated bat position
     */
    @JvmStatic
    fun getBatPositionFromName(
        eye: Location,
        roll: Double,
        rollDirection: Int,
        batName: String
    ): Location {
        val eyeLoc = eye.clone()
        // 理想のミートポイントから90度スイングを戻すと始点になる。今回のインパクトは始点からどれだけ回った位置だったか
        eyeLoc.yaw -= (90 * rollDirection - Math.toDegrees(roll)).toFloat()
        // スイングの水平方向成分
        val push = eyeLoc.getDirection().setY(0).normalize()
        // アッパースイング度を理想のミートポイントがスイングの最下点よりどれだけ前/後かと定義している
        val upper = BatConfig[batName]?.fly ?: 0.0
        // 始点から、最速降下曲線を回転させるようにしてスイングしていく
        val theta = abs(roll * 2) + Math.PI * upper
        val x = push.normalize().getX() * (theta - sin(theta))
        val y = -(1 - cos(theta))
        val z = push.normalize().getZ() * (theta - sin(theta))
        return eye.clone().add(x, y, z)
    }

    /**
     * Retrieves ball movement values from configuration based on ball name.
     *
     * @deprecated use getBallAttributes
     * @param ballName The name of the ball type
     * @param velocity Current velocity vector
     * @param isRightHanded Whether the pitcher is right-handed
     * @param isFromDispenser Whether the ball is from a dispenser
     * @return HashMap containing vModifier, spinVector, acceleration, random, and tracker
     */
    @JvmStatic
    fun getBallValuesFromName(
        ballName: String,
        velocity: Vector,
        isRightHanded: Boolean,
        isFromDispenser: Boolean
    ): HashMap<String, Any?> {
        val (vModifier, spinVector, acceleration, random, tracker) = BallAttributesCalculator.calc(
            ballName = ballName,
            velocity = velocity,
            isRightHanded = isRightHanded,
            isFromDispenser = isFromDispenser
        )
        val values = HashMap<String, Any?>()
        values["vModifier"] = vModifier
        values["spinVector"] = spinVector
        values["acceleration"] = acceleration
        values["random"] = random
        values["tracker"] = tracker
        return values
    }
    /**
     * Retrieves ball movement values from configuration based on ball name.
     *
     * @param ballName The name of the ball type
     * @param velocity Current velocity vector
     * @param isRightHanded Whether the pitcher is right-handed
     * @param isFromDispenser Whether the ball is from a dispenser
     * @return HashMap containing vModifier, spinVector, acceleration, random, and tracker
     */
    @JvmStatic
    fun getBallAttributes(
        ballName: String,
        velocity: Vector,
        isRightHanded: Boolean,
        isFromDispenser: Boolean
    ): BallAttributes {
        return BallAttributesCalculator.calc(
            ballName = ballName,
            velocity = velocity,
            isRightHanded = isRightHanded,
            isFromDispenser = isFromDispenser
        )
    }

    /**
     * Gets position and velocity modifiers based on glove configuration.
     *
     * @deprecated use getGloveAttributes
     * @param name Glove type name
     * @param eye Player eye location
     * @param hasGloveOnLeft Whether the glove is on left hand
     * @return HashMap containing "rp" (release point modifier) and "velocity" (velocity modifier)
     */
    @JvmStatic
    fun getModifiersFromGloveName(
        name: String,
        eye: Location,
        hasGloveOnLeft: Boolean
    ): HashMap<String, Vector> {
        val (releasePointModifier, velocityModifier) = GloveAttributesCalculator.calc(
            name = name,
            eye = eye,
            hasGloveOnLeft = hasGloveOnLeft
        )
        val values = HashMap<String, Vector>()
        values["rp"] = releasePointModifier
        values["velocity"] = velocityModifier
        return values
    }

    /**
     * Gets position and velocity modifiers based on glove configuration.
     *
     * @param name Glove type name
     * @param eye Player eye location
     * @param hasGloveOnLeft Whether the glove is on left hand
     * @return HashMap containing "rp" (release point modifier) and "velocity" (velocity modifier)
     */
    @JvmStatic
    fun getGloveAttributes(
        name: String,
        eye: Location,
        hasGloveOnLeft: Boolean
    ): GloveAttributes {
        return GloveAttributesCalculator.calc(
            name = name,
            eye = eye,
            hasGloveOnLeft = hasGloveOnLeft
        )
    }

    /**
     * Checks if the given item is a Ball item.
     * @param item The item to check
     * @return true if the item is a Ball item, false otherwise
     */
    @JvmStatic
    fun isBallItem(item: ItemStack): Boolean {
        return BallItem.from(item) != null
    }

    /**
     * Creates a new Ball item.
     * @param ballType The type of ball (e.g., "Highest", "Higher", "Normal", "Lower", "Lowest"). Defaults to "Normal"
     * @return A new Ball ItemStack
     */
    @JvmStatic
    fun getBallItem(ballType: String? = null): ItemStack {
        return BallItem.generate(ballType ?: "Normal").item
    }

    /**
     * Checks if the given item is a Bat item.
     * @param item The item to check
     * @return true if the item is a Bat item, false otherwise
     */
    @JvmStatic
    fun isBatItem(item: ItemStack): Boolean {
        return BatItem.from(item) != null
    }

    /**
     * Creates a new Bat item.
     * @return A new Bat ItemStack
     */
    @JvmStatic
    fun getBatItem(): ItemStack {
        return BatItem.generate().item
    }

    /**
     * Checks if the given item is a Glove item.
     * @param item The item to check
     * @return true if the item is a Glove item, false otherwise
     */
    @JvmStatic
    fun isGloveItem(item: ItemStack): Boolean {
        return GloveItem.from(item) != null
    }

    /**
     * Creates a new Glove item.
     * @return A new Glove ItemStack
     */
    @JvmStatic
    fun getGloveItem(): ItemStack {
        return GloveItem.generate().item
    }

    /**
     * Checks if the given item is an Umpire item.
     * @param item The item to check
     * @return true if the item is an Umpire item, false otherwise
     */
    @JvmStatic
    fun isUmpireItem(item: ItemStack): Boolean {
        return UmpireItem.from(item) != null
    }

    /**
     * Creates a new Umpire item.
     * @return A new Umpire ItemStack
     */
    @JvmStatic
    fun getUmpireItem(): ItemStack {
        return UmpireItem.generate().item
    }

    /**
     * Checks if the given item is a Base item.
     * @param item The item to check
     * @return true if the item is a Base item, false otherwise
     */
    @JvmStatic
    fun isBaseItem(item: ItemStack): Boolean {
        return BaseItem.from(item) != null
    }

    /**
     * Creates a new Base item.
     * @return A new Base ItemStack
     */
    @JvmStatic
    fun getBaseItem(): ItemStack {
        return BaseItem.generate().item
    }

    /**
     * Checks if the given item is a Coach item.
     * @param item The item to check
     * @return true if the item is a Coach item, false otherwise
     */
    @JvmStatic
    fun isCoachItem(item: ItemStack): Boolean {
        return CoachItem.from(item) != null
    }

    /**
     * Creates a new Coach item.
     * @return A new Coach ItemStack
     */
    @JvmStatic
    fun getCoachItem(): ItemStack {
        return CoachItem.generate().item
    }

    @JvmStatic
    fun isBall(projectile: Projectile) : Boolean{
        return Ball.from(projectile) != null
    }

    @JvmStatic
    fun isUmpire(armorStand: ArmorStand) : Boolean{
        return Umpire.from(armorStand) != null
    }

    @JvmStatic
    fun isBase(armorStand: ArmorStand) : Boolean{
        return Base.from(armorStand) != null
    }

    @JvmStatic
    fun isBallmaker(snowman: Snowman): Boolean {
        return Ballmaker.from(snowman) != null
    }

}
