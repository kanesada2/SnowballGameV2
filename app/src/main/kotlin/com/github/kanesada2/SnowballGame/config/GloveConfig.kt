package com.github.kanesada2.SnowballGame.config

import org.bukkit.configuration.file.FileConfiguration

data class GloveValues(
    val vertical: Double,
    val horizontal: Double,
    val closeness: Double,
    val catchRange: Double
)

object GloveConfig : ConfigSection {
    var enabled = false
        private set
    var name = "Glove"
        private set
    var types: List<String> = emptyList()
        private set
    var gloveValues: Map<String, GloveValues> = emptyMap()
        private set
    override fun load(config: FileConfiguration){
        val section = config.getConfigurationSection("Glove") ?: return
        enabled = section.getBoolean("Enabled_Glove")
        name = section.getString("Glove_Name") ?: name
        types = section.getStringList("Custom.Type")

        gloveValues = types.associateWith { type ->
            val typeSection = section.getConfigurationSection("Custom.$type")
            GloveValues(
                vertical = typeSection?.getDouble("Vertical") ?: 0.0,
                horizontal = typeSection?.getDouble("Horizontal") ?: 0.0,
                closeness = typeSection?.getDouble("Closeness") ?: 0.0,
                catchRange = typeSection?.getDouble("CatchRange") ?: 3.0
            )
        }
    }
    operator fun get(type: String): GloveValues? = gloveValues[type]
}