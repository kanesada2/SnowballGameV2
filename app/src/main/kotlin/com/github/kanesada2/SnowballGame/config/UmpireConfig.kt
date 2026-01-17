package com.github.kanesada2.SnowballGame.config

import org.bukkit.configuration.file.FileConfiguration

object UmpireConfig : ConfigSection {
    var enabled: Boolean = false
        private set
    var name: String = "Umpire"
    var top: Double = 1.7
        private set
    var bottom: Double = 0.5
    override fun load(config: FileConfiguration){
        val section = config.getConfigurationSection("Umpire")
        enabled = section?.getBoolean("Enabled_Umpire")?: enabled
        name = section?.getString("Umpire_Name") ?: name
        top = section?.getDouble("Top")?: top
        bottom = section?.getDouble("Bottom")?: bottom
    }
}