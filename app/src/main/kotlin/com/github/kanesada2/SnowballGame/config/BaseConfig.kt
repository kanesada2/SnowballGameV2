package com.github.kanesada2.SnowballGame.config

import org.bukkit.configuration.file.FileConfiguration

object BaseConfig : ConfigSection {
    var enabled: Boolean = false
        private set
    var name: String = "Base"
    override fun load(config: FileConfiguration){
        val section = config.getConfigurationSection("Base")
        enabled = section?.getBoolean("Enabled_Base")?: false
        name = section?.getString("Base_Name") ?: name
    }
}