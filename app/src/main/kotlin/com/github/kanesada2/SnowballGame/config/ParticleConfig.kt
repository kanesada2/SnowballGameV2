package com.github.kanesada2.SnowballGame.config

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.block.data.BlockData
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.FileConfiguration

// ParticleSettings.kt
data class ParticleSettings(
    val enabled: Boolean = false,
    val particle: Particle? = null,
    val block: Material? = null,
    val time: Int = 200
)

enum class ParticleType(val configKey: String) {
    CATCH_BALL("Catch_Ball"),
    THROW_BALL("Throw_Ball"),
    HIT_TO_PLAYERS("Hit_to_Players"),
    SWING_BAT("Swing_Bat"),
    SWING_BAT_SEQUENT("Swing_Bat_Sequent"),
    HIT_BY_BAT("Hit_by_Bat"),
    BATTED_BALL_IN_FLIGHT("BattedBall_InFlight"),
    BATTED_BALL_GROUND("BattedBall_Ground"),
    MOVING_BALL("MovingBall")
}

// ParticleConfig.kt
object ParticleConfig : ConfigSection {

    private var settings: Map<ParticleType, ParticleSettings> = emptyMap()

    override fun load(config: FileConfiguration) {
        val section = config.getConfigurationSection("Particle") ?: return

        settings = ParticleType.entries.associateWith { type ->
            val typeSection = section.getConfigurationSection(type.configKey)
            ParticleSettings(
                enabled = typeSection?.getBoolean("Enabled") ?: false,
                particle = typeSection?.let{extractParticle(typeSection)},
                block = typeSection?.let{ extractParticleBlock(typeSection) },
                time = typeSection?.getInt("Time")?.takeIf { it > 0 }?: 200
            )
        }
    }

    fun extractParticle (config: ConfigurationSection): Particle? {
        var particle: Particle? = null
        if (config.contains("Particle")) {
            try {
                particle = config.getString("Particle")?.let { Particle.valueOf(it) }
            } catch (e: IllegalArgumentException) {
                Bukkit.broadcastMessage(
                    "The value of " + config.currentPath + ".Particle : " + config.getString(
                        "Particle"
                    ) + " is invalid!!"
                )
            }
        }
        return particle
    }
    fun extractParticleBlock (config: ConfigurationSection): Material? {
        var material: Material? = null
        if (config.contains("Block")) {
            try {
                material = config.getString("Block")?.let { Material.valueOf(it) }
                if(!(material?.isBlock?:false)) throw java.lang.IllegalArgumentException()
            } catch (e: IllegalArgumentException) {
                Bukkit.broadcastMessage(
                    "The value of " + config.currentPath + ".Block : " + config.getString(
                        "Block"
                    ) + " is invalid!!"
                )
            }
        }
        return material
    }

    operator fun get(type: ParticleType): ParticleSettings =
        settings[type] ?: ParticleSettings()

    fun isEnabled(type: ParticleType): Boolean = this[type].enabled

    fun spawnIfEnabled(
        type: ParticleType,
        location: Location,
        count: Int = 1,
        offsetX: Double = 0.0,
        offsetY: Double = 0.0,
        offsetZ: Double = 0.0
    ) {
        val config = this[type]
        if (!config.enabled) return
        if(config.particle == null) return

        val world = location.world ?: return

        if (config.particle.dataType == BlockData::class.java && config.block != null) {
            world.spawnParticle(
                config.particle,
                location,
                count,
                offsetX,
                offsetY,
                offsetZ,
                config.block.createBlockData()
            )
        } else {
            world.spawnParticle(
                config.particle,
                location,
                count,
                offsetX,
                offsetY,
                offsetZ
            )
        }
    }
}

