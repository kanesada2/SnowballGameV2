package com.github.kanesada2.SnowballGame

import org.bukkit.NamespacedKey
import org.bukkit.entity.Player

object PersistentDataKeys {
    private lateinit var plugin: SnowballGame
    fun init(plugin: SnowballGame) {
        this.plugin = plugin
    }

    fun publishSwingGaugeKey(player: Player) : NamespacedKey {
        return NamespacedKey(plugin, "swing_gauge_${player.uniqueId}")
    }

    val BallType by lazy { NamespacedKey(plugin, "BallType") }
    val Bat by lazy { NamespacedKey(plugin, "Bat") }
    val Glove by lazy { NamespacedKey(plugin, "Glove") }
    val Umpire by lazy { NamespacedKey(plugin, "Umpire") }
    val Coach by lazy { NamespacedKey(plugin, "Coach") }
    val Base by lazy { NamespacedKey(plugin, "Base") }
}