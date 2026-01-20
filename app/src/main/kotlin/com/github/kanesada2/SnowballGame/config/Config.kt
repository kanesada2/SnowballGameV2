package com.github.kanesada2.SnowballGame.config

import com.github.kanesada2.SnowballGame.SnowballGame
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

interface ConfigSection {
    fun load(config: FileConfiguration)
}

object ConfigLoader {
    lateinit var plugin: SnowballGame;
    private val sections = listOf(
        BallConfig,
        BallmakerConfig,
        BaseConfig,
        BatConfig,
        CoachConfig,
        GloveConfig,
        MessageConfig,
        ParticleConfig,
        UmpireConfig
    )

    fun init(plugin: SnowballGame) {
        this.plugin = plugin
    }

    fun load() {
        plugin.saveDefaultConfig()
        plugin.config.options().copyDefaults(true)
        plugin.saveConfig()
        reload()
    }

    fun reload() {
        plugin.reloadConfig()

        val bounce = loadCustomConfig(plugin, "bounce.yml")
        BounceConfig.load(bounce)
        sections.forEach { it.load(plugin.config) }
    }

    fun loadCustomConfig(plugin: JavaPlugin, fileName: String): FileConfiguration {
        val file = File(plugin.dataFolder, fileName)

        // デフォルトファイルがなければresourcesからコピー
        if (!file.exists()) {
            plugin.saveResource(fileName, false)
        }

        return YamlConfiguration.loadConfiguration(file)
    }
}