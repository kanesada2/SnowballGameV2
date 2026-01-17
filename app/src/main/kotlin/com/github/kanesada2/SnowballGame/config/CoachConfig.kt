package com.github.kanesada2.SnowballGame.config

import org.bukkit.configuration.file.FileConfiguration

object CoachConfig : ConfigSection {
    var enabled: Boolean = false
        private set
    var name: String = "Coach"
        private set
    var range: Double = 120.0
    override fun load(config: FileConfiguration){
        val section = config.getConfigurationSection("Coach")
        enabled = section?.getBoolean("Enabled_Coach")?: false
        name = section?.getString("Coach_Name") ?: name
        range = section?.getDouble("Coach_Range")?: range
    }
}