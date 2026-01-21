package com.github.kanesada2.SnowballGame

import com.github.kanesada2.SnowballGame.config.BallConfig
import com.github.kanesada2.SnowballGame.entity.*
import com.github.kanesada2.SnowballGame.extension.BukkitRunnableExtension.repeat
import com.github.kanesada2.SnowballGame.item.BatItem
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.inventory.MainHand
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import org.bukkit.util.Vector
import java.util.UUID

class TesterListener : Listener {

    private val testerTasks = mutableMapOf<UUID, BukkitTask>()

    @EventHandler
    fun onBatterInteract(e: PlayerInteractEvent) {
        if (e.action != Action.RIGHT_CLICK_AIR && e.action != Action.RIGHT_CLICK_BLOCK) return
        Batter.from(e.player)?.let { batter ->
            if (batter.batItem.name == "TESTER") {
                e.isCancelled = true
                startTesterMode(e.player)
            }
        }
    }

    @EventHandler
    fun onSwitchFromBat(e: PlayerItemHeldEvent) {
        val previousItem = e.player.inventory.getItem(e.previousSlot) ?: return
        BatItem.from(previousItem)?.let {
            if (it.name == "TESTER") {
                stopTesterMode(e.player)
            }
        }
    }

    private fun startTesterMode(player: Player) {
        stopTesterMode(player)

        val task = object : BukkitRunnable() {
            override fun run() {
                val batItem = BatItem.from(player.inventory.itemInMainHand)
                    ?: BatItem.from(player.inventory.itemInOffHand)
                if (batItem == null || batItem.name != "TESTER") {
                    stopTesterMode(player)
                    return
                }

                val currentPower = 1.0
                val lengthModifier = currentPower * 0.5 + 0.5
                val toCenter = player.location.direction.normalize().multiply(batItem.length * lengthModifier)
                val center = player.eyeLocation.add(toCenter)
                val force = currentPower * batItem.power
                val size = batItem.range + (1 - currentPower) * Constants.Batter.HIT_RANGE_MODIFIER * batItem.range
                val range = Vector(1, 1, 1).multiply(size)

                val ballTypes = BallConfig.getRepulsionTypes().ifEmpty { listOf("Normal") }
                val randomBallType = ballTypes.random()
                val randomOffset = Vector(
                    (Math.random() - 0.5) * 2.4,
                    (Math.random() - 0.5) * 2.4,
                    (Math.random() - 0.5) * 2.4
                )
                val launchLocation = center.clone().add(randomOffset)
                val randomVelocity = Vector(
                    (Math.random() - 0.5) * 2.0,
                    (Math.random() - 0.5) * 2.0,
                    (Math.random() - 0.5) * 2.0
                )

                Ball.launch(
                    LaunchSettings(
                        shooter = player,
                        velocity = randomVelocity,
                        rPoint = launchLocation,
                        ballType = randomBallType,
                        ballName = randomBallType
                    )
                )

                val bat = Bat.spawn(center)
                val isMainHanded = BatItem.from(player.inventory.itemInMainHand) != null
                val isRight = player.mainHand == MainHand.RIGHT && isMainHanded ||
                        player.mainHand == MainHand.LEFT && !isMainHanded
                val rollDirection = if (isRight) -1 else 1
                val eyeLoc = player.eyeLocation.clone()
                eyeLoc.yaw -= (90 * rollDirection - Math.toDegrees((Math.PI / 2 + Constants.Batter.IMPACT_ADJUSTMENT) * rollDirection)).toFloat()
                val push1 = eyeLoc.direction.setY(0).normalize()
                val theta1 = kotlin.math.abs((Math.PI / 2 + Constants.Batter.IMPACT_ADJUSTMENT) * rollDirection * 2) + Math.PI * batItem.fly
                val pos1 = player.eyeLocation.clone().add(
                    push1.x * (theta1 - kotlin.math.sin(theta1)),
                    -(1 - kotlin.math.cos(theta1)),
                    push1.z * (theta1 - kotlin.math.sin(theta1))
                )
                val eyeLoc2 = player.eyeLocation.clone()
                eyeLoc2.yaw -= (90 * rollDirection - Math.toDegrees(Math.PI / 2 * rollDirection)).toFloat()
                val push2 = eyeLoc2.direction.setY(0).normalize()
                val theta2 = kotlin.math.abs(Math.PI / 2 * rollDirection * 2) + Math.PI * batItem.fly
                val pos2 = player.eyeLocation.clone().add(
                    push2.x * (theta2 - kotlin.math.sin(theta2)),
                    -(1 - kotlin.math.cos(theta2)),
                    push2.z * (theta2 - kotlin.math.sin(theta2))
                )
                val batMove = pos1.subtract(pos2).toVector().normalize()

                bat.swing(
                    player = player,
                    batItem = batItem.item,
                    force = force,
                    range = range,
                    batMove = batMove
                )
                bat.remove()
            }
        }.repeat(0, 100)

        testerTasks[player.uniqueId] = task
        player.sendMessage("TESTER mode started (100 tick interval)")
    }

    private fun stopTesterMode(player: Player) {
        testerTasks.remove(player.uniqueId)?.cancel()
    }
}
