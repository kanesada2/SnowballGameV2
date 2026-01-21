package com.github.kanesada2.SnowballGame

import com.github.kanesada2.SnowballGame.api.BallBounceEvent
import com.github.kanesada2.SnowballGame.api.BallHitEvent
import com.github.kanesada2.SnowballGame.api.PlayerHitBallEvent
import org.bukkit.Location
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.Vector
import java.io.File
import java.util.UUID
import kotlin.math.atan2
import kotlin.math.sqrt

data class BattedBallData(
    val initialLocation: Location,
    val speedKmh: Double,
    val angleDegrees: Double
)

class BattedBallRecorderListener(private val plugin: JavaPlugin) : Listener {

    private val battedBalls = mutableMapOf<UUID, BattedBallData>()
    private val csvFile: File by lazy {
        plugin.dataFolder.mkdirs()
        File(plugin.dataFolder, "batted.csv").also { file ->
            if (!file.exists()) {
                file.writeText("speed_kmh,angle_deg,distance_m\n")
            }
        }
    }

    @EventHandler
    fun onHit(e: BallHitEvent) {
        val velocity = e.entity.velocity
        val speedKmh = velocity.length() * Constants.BallJudge.VELOCITY_TO_KMH
        val horizontalSpeed = sqrt(velocity.x * velocity.x + velocity.z * velocity.z)
        val angleDegrees = Math.toDegrees(atan2(velocity.y, horizontalSpeed))
        val initialLocation = e.entity.location.clone()

        battedBalls[e.entity.uniqueId] = BattedBallData(
            initialLocation = initialLocation,
            speedKmh = speedKmh,
            angleDegrees = angleDegrees
        )
    }

    @EventHandler
    fun onBounce(e: BallBounceEvent) {
        if (!e.isFirst) return

        val data = battedBalls.remove(e.beforeBounce.uniqueId) ?: return
        val bounceLocation = e.beforeBounce.location
        val distance = data.initialLocation.distance(bounceLocation)

        val line = "%.2f,%.2f,%.2f\n".format(data.speedKmh, data.angleDegrees, distance)
        csvFile.appendText(line)
    }
}
