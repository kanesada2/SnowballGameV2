package com.github.kanesada2.SnowballGame.config

import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.FileConfiguration

// BallMoveSettings.kt
data class BallMoveSettings(
    val velocity: Double = 1.0,
    val vertical: Double = 0.0,
    val horizontal: Double = 0.0,
    val acceleration: Double = 0.0,
    val random: Double = 0.0,
    val tracker: Particle? = null,
    val trackerBlock: Material? = null,
    val reusable: Boolean = false
)

// BallRepulsionSettings.kt
data class BallRepulsionSettings(
    val coefficient: Double = 1.0,
    val name: String = "Ball"
)

// BallConfig.kt
object BallConfig : ConfigSection {

    var enabled: Boolean = true
        private set
    var defaultName: String = "Ball"
        private set
    var coolTime: Int = 30
        private set

    private var moveTypes: List<String> = emptyList()
    private var moveSettings: Map<String, BallMoveSettings> = emptyMap()
    private var repulsionSettings: Map<String, BallRepulsionSettings> = emptyMap()

    override fun load(config: FileConfiguration) {
        val section = config.getConfigurationSection("Ball") ?: return

        enabled = section.getBoolean("Enabled_Ball", true)
        defaultName = section.getString("Ball_Name") ?: defaultName
        coolTime = section.getInt("Cool_Time", 30)

        loadMoveSettings(section.getConfigurationSection("Move"))
        loadRepulsionSettings(section.getConfigurationSection("Repulsion_New"))
    }

    private fun loadMoveSettings(section: ConfigurationSection?) {
        section ?: return

        moveTypes = section.getStringList("Type")

        moveSettings = moveTypes.associateWith { type ->
            val typeSection = section.getConfigurationSection(type)
            BallMoveSettings(
                velocity = typeSection?.getDouble("Velocity", 1.0) ?: 1.0,
                vertical = typeSection?.getDouble("Vertical") ?: 0.0,
                horizontal = typeSection?.getDouble("Horizontal") ?: 0.0,
                acceleration = typeSection?.getDouble("Acceleration") ?: 0.0,
                random = typeSection?.getDouble("Random") ?: 0.0,
                tracker = typeSection?.let{ ParticleConfig.extractParticle(it)},
                trackerBlock = typeSection?.let { ParticleConfig.extractParticleBlock(it) },
                reusable = typeSection?.getBoolean("Reusable") ?: false
            )
        }
    }

    private fun loadRepulsionSettings(section: ConfigurationSection?) {
        section ?: return

        repulsionSettings = section.getKeys(false).associateWith { type ->
            val typeSection = section.getConfigurationSection(type)
            BallRepulsionSettings(
                coefficient = typeSection?.getDouble("Coefficient") ?: 1.0,
                name = typeSection?.getString("Name") ?: "Ball"
            )
        }
    }

    // Move関連
    fun getMoveTypes(): List<String> = moveTypes

    fun getMove(type: String): BallMoveSettings =
        moveSettings[type] ?: BallMoveSettings()

    // Repulsion関連
    fun getRepulsionTypes(): List<String> = repulsionSettings.keys.toList()

    fun getRepulsion(type: String): BallRepulsionSettings =
        repulsionSettings[type] ?: BallRepulsionSettings()

    fun getCoefficient(type: String): Double = getRepulsion(type).coefficient
}