package com.github.kanesada2.SnowballGame.config

import org.bukkit.configuration.file.FileConfiguration

object BallmakerConfig : ConfigSection {
    var enabled: Boolean = false
        private set
    var name: String = "Ball_Smith"
    override fun load(config: FileConfiguration){
        val section = config.getConfigurationSection("Ballmaker")
        enabled = section?.getBoolean("Enabled_Ballmaker")?: enabled
        name = section?.getString("Ballmaker_Name") ?: name
    }
}