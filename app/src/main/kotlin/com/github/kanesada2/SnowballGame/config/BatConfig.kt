package com.github.kanesada2.SnowballGame.config

import org.bukkit.configuration.file.FileConfiguration

data class BatValues(
    val fly: Double,
    val range: Double,
    val length: Double,
    val power: Double
)

object BatConfig : ConfigSection {
    var enabled = false
        private set
    var types: List<String> = emptyList()
        private set
    var BatValues: Map<String, BatValues> = emptyMap()
        private set
    var name: String = "Bat"
    override fun load(config: FileConfiguration){
        val section = config.getConfigurationSection("Bat") ?: return
        enabled = section.getBoolean("Enabled_Bat")
        name = section.getString("Bat_Name")?: name

        types = section.getStringList("Swing.Type")

        BatValues = types.associateWith { type ->
            val typeSection = section.getConfigurationSection("Swing.$type")
            BatValues(
                fly = typeSection?.getDouble("Fly") ?: 0.0,
                range = typeSection?.getDouble("Range", 1.2) ?: 1.2,
                length = typeSection?.getDouble("Length", 3.0) ?: 3.0,
                power = (typeSection?.getDouble("Power", 1.0) ?: 1.0).coerceAtLeast(0.01)
            )
        }
    }
    operator fun get(type: String): BatValues? = BatValues[type]
}