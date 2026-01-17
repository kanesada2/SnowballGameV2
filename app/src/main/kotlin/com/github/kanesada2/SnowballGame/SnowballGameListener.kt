package com.github.kanesada2.SnowballGame

import com.github.kanesada2.SnowballGame.api.BallBounceEvent
import com.github.kanesada2.SnowballGame.api.PlayerCatchBallEvent
import com.github.kanesada2.SnowballGame.api.PlayerHitBallEvent
import com.github.kanesada2.SnowballGame.api.PlayerSwingBatEvent
import com.github.kanesada2.SnowballGame.api.PlayerThrowBallEvent
import com.github.kanesada2.SnowballGame.config.*
import com.github.kanesada2.SnowballGame.entity.*
import com.github.kanesada2.SnowballGame.item.BatItem
import com.github.kanesada2.SnowballGame.item.CoachItem
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.BlockFace
import org.bukkit.block.Dispenser
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDispenseEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerArmorStandManipulateEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.projectiles.BlockProjectileSource
import kotlin.math.roundToInt


/**
 * 実態のある処理は基本的に各Entityに任せる
 * 演出面（Broadcast, Particle, Sound）は極力こっち（一部例外あり）
 */
class SnowballGameListener: Listener {

    @EventHandler
    fun onLaunch(e: ProjectileLaunchEvent) {
        if(e.entity !is Snowball) return
        val shooter = e.entity.shooter
        if(shooter is Player) {
            val thrower = Thrower.from(shooter)?: return
            e.isCancelled = true
            thrower.pitch(e.entity)
            return
        }
        if(shooter is BlockProjectileSource){
            val dispenser = shooter.block.state as? Dispenser ?: return
            e.isCancelled = true
            PitchingMachine.from(dispenser)?.pitch(e.entity)
            return
        }
        if(shooter == null){
            // due to maybe a spigot bug, shooter is null when projectile was shot from dispenser (in 1.21)
            val sourceBlock = e.entity.velocity.multiply(-1).let { v ->
                e.location.block.getRelative(v.x.roundToInt(), v.y.roundToInt(), v.z.roundToInt())
            }
            val dispenser = sourceBlock.state as? Dispenser ?: return
            PitchingMachine.from(dispenser)?.let{
                e.isCancelled = true
                it.pitch(e.entity)
            }
            return
        }
    }

    @EventHandler
    fun onDispense(e: BlockDispenseEvent) {
        val dispenser = e.block.state as? Dispenser ?: return
        PitchingMachine.mark(dispenser, e.item)
    }

    @EventHandler
    fun onProjectileHit(e: ProjectileHitEvent) {
        Ball.from(e.entity)?.let { ball ->
            e.hitBlock?.let {
                ball.bounce(BounceSettings(hitBlock = it))
                return
            }
            e.hitEntity?.let {entity ->
                ball.hitToEntity(entity)
            }
        }
    }

    @EventHandler
    fun onBatterInteract(e: PlayerInteractEvent) {
        if(e.action != Action.RIGHT_CLICK_AIR && e.action != Action.RIGHT_CLICK_BLOCK) return
        Batter.from(e.player)?.let {
            e.isCancelled = true
            it.swingSequence()
        }
    }

    @EventHandler
    fun onSwitchFromBat(e: PlayerItemHeldEvent){
        val previousItem = e.player.inventory.getItem(e.previousSlot) ?: return
        BatItem.from(previousItem)?.let{ Batter.force(e.player) }.apply {
            this?.stopSwingGauge()
            this?.removeSwingGauge()
        }
    }

    @EventHandler
    fun onClickInventory(e: InventoryClickEvent) {
        (e.whoClicked as? Player)?.let { Batter.from(it); }?.let {
            it.stopSwingGauge()
            it.removeSwingGauge()
        }
    }

    @EventHandler
    fun onThrowerInteract(e: PlayerInteractEvent) {
        if(e.action != Action.RIGHT_CLICK_BLOCK || e.clickedBlock?.type != Material.BREWING_STAND) return
        Thrower.from(e.player)?.let {
            e.isCancelled = true
            it.placeOn(e.clickedBlock)
        }
    }

    @EventHandler
    fun onSwap(e: PlayerSwapHandItemsEvent){
        Thrower.from(e.player)?.let {
            e.isCancelled = true
            it.toss()
            e.player.world.playSound(e.player.eyeLocation, Sound.ENTITY_SNOWBALL_THROW , 0.5f, 0.0f)
        }
    }

    @EventHandler
    fun onFielderInteract(e: PlayerInteractEvent) {
        if(e.action != Action.RIGHT_CLICK_AIR && e.action != Action.RIGHT_CLICK_BLOCK) return
        Fielder.from(e.player)?.let {
            if(!it.isReadyToCatch) return
            it.tryCatch()
        }
    }

    @EventHandler
    fun onTag(e: EntityDamageByEntityEvent) {
        if(e.entity !is Player || e.damager !is Player) return
        Fielder.from(e.damager as Player)?.let {
            e.isCancelled = true
            it.tag(e.entity as Player)
            return
        }
        Batter.from(e.damager as Player)?.let {
            e.isCancelled = true
            val standingOn = e.entity.location.block
            val below = e.entity.location.block.getRelative(BlockFace.DOWN)
            (Base.from(standingOn)?:Base.from(below))?.let{ base ->
                base.handleLeftClick(it.player)
                return
            }
            it.player.sendMessage("OOPS! Don't swing at anything worth more than the dugout phone!")
        }
    }

    @EventHandler
    fun onSneak(e: PlayerToggleSneakEvent) {
        if(e.isSneaking) {
            Fielder.from(e.player)?.appeal()
            return
        }
        // スニーク解除時にダッシュしたらダイブ
        if(! e.player.isSprinting) return
        // 双方ともにCanSlideを実装しているので
        (Fielder.from(e.player) ?: Batter.from(e.player))?.slide()
    }

    @EventHandler
    fun onCoachSummonerInteract(e: PlayerInteractEvent) {
        if(!(e.action == Action.RIGHT_CLICK_AIR || e.action == Action.RIGHT_CLICK_BLOCK) || CoachItem.from(e.player.inventory.itemInMainHand) == null) return
        if(!CoachConfig.enabled) return
        e.isCancelled = true
        val nextBlock = e.clickedBlock?.getRelative(e.blockFace)?: return
        Coach.spawn(e.player, nextBlock.location)
    }

    @EventHandler
    fun onCoachDamaged(e: EntityDamageEvent) {
        if(e.entity !is ArmorStand) return
        Coach.from(e.entity as ArmorStand)?.let {
            e.isCancelled = true
            if(e.cause == EntityDamageEvent.DamageCause.ENTITY_ATTACK){
                it.remove()
            }
        }
    }

    @EventHandler
    fun onCoachManipulated(e: PlayerArmorStandManipulateEvent) {
        Coach.from(e.rightClicked)?.let {
            e.isCancelled = true
            val item = it.changeCloth(e.playerItem, e.slot)
            if(e.player.gameMode == GameMode.CREATIVE) return
            e.player.inventory.setItemInMainHand(item)
        }
    }

    @EventHandler
    fun onBaseInteracted(e: PlayerInteractEvent) {
        if(e.action != Action.LEFT_CLICK_BLOCK) return
        Base.from(e.clickedBlock)?.let {
            // メインハンドにバットかオフハンドにグラブを持っているプレイヤーはベースを壊せない
            e.isCancelled = it.handleLeftClick(e.player)
        }
    }

    @EventHandler
    fun onBasePlaced(e : BlockPlaceEvent) {
        BasePlacer.from(e.player)?.place(e.block)
    }
    @EventHandler
    fun onBaseBroken(e: BlockBreakEvent){
        Base.from(e.block)?.let{
            e.isDropItems = false
            it.breakWithBlock(e.block)
        }
    }

    @EventHandler
    fun onBallMakerSet(e: EntityDamageByEntityEvent){
        if(e.entity !is Snowman || e.damager !is Player) return
        Thrower.from(e.damager as Player)?.let {
            e.isCancelled = true
            Ballmaker.mark(e.entity as Snowman, it.mainHand)
        }
    }

    @EventHandler
    fun onBallSnowDig(e: BlockBreakEvent){
        // 「雪だるまの作った雪」ではなく、「そのとき雪だるまの足元にある雪」なので注意
        e.block.world.getNearbyEntities(e.block.location, 0.5,0.5,0.5)
            .filterIsInstance<Snowman>()
            .firstNotNullOfOrNull(Ballmaker::from)?.let {
                e.isDropItems = false
                it.breakAndDrop(e.block)
            }
    }

    // 以下、SBG独自イベント
    @EventHandler
    fun onThrow(e: PlayerThrowBallEvent){
        ParticleConfig.spawnIfEnabled(ParticleType.THROW_BALL, e.rPoint)
        e.player.world.playSound(e.rPoint, Sound.ENTITY_SNOWBALL_THROW , 1.0f, 0.0f)
    }

    @EventHandler
    fun onCatch(e: PlayerCatchBallEvent){
        Fielder.from(e.player)?.let {fielder ->
            // ダイレクトキャッチ時
            ParticleConfig.spawnIfEnabled(ParticleType.CATCH_BALL, e.ball.location)
            if(e.isDirect) MessageConfig.broadcast(MessageType.CATCH, e.player.location, "PLAYER" to e.player.displayName)
            // ベースの上での捕球時（同時に発生して併殺もあるのでreturnしない）
            val standingOn = e.player.location.block
            val below = e.player.location.block.getRelative(BlockFace.DOWN)
            (Base.from(standingOn) ?: Base.from(below))?.let {
                fielder.appeal()
            }
        }
    }

    @EventHandler
    fun onSwing(e: PlayerSwingBatEvent){
        if(e.force > 0.4){
            MessageConfig.broadcast(MessageType.SWING, e.player.location, "PLAYER" to e.player.displayName)
            ParticleConfig.spawnIfEnabled(ParticleType.SWING_BAT, e.center)
        }else {
            e.player.world.playSound(e.center, Sound.ENTITY_PLAYER_ATTACK_SWEEP, e.force, 1.0f)
            MessageConfig.broadcast(MessageType.BUNT, e.player.location, "PLAYER" to e.player.displayName)
        }
    }

    @EventHandler
    fun onHit(e: PlayerHitBallEvent){
        MessageConfig.broadcast(MessageType.HIT, e.player.location, "PLAYER" to e.player.displayName)
        ParticleConfig.spawnIfEnabled(ParticleType.HIT_BY_BAT, e.beforeHit.location)
        e.player.world.playSound(e.beforeHit.location, Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, e.velocity.length().coerceIn(0.0, 1.0).toFloat(), 1.0f)
    }

    @EventHandler
    fun onGround(e: BallBounceEvent) {
        if(!e.isFirst || !ParticleConfig.isEnabled(ParticleType.BATTED_BALL_GROUND)) return
        val config = ParticleConfig[ParticleType.BATTED_BALL_GROUND]
        config.particle?.let {
            val cloud = e.beforeBounce.world.spawnEntity(e.beforeBounce.location, EntityType.AREA_EFFECT_CLOUD) as AreaEffectCloud
            cloud.particle = it
            cloud.duration = config.time
            cloud.radius = 1.5f
        }
    }


}