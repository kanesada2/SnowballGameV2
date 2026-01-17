package com.github.kanesada2.SnowballGame.extension

import com.github.kanesada2.SnowballGame.SnowballGame
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask

object BukkitRunnableExtension {
    lateinit var plugin: SnowballGame
        private set

    fun init(plugin: SnowballGame) {
        this.plugin = plugin
    }

    fun BukkitRunnable.repeat(delay: Int, period: Long): BukkitTask{
        return this.runTaskTimer(plugin, delay.toLong(), period)
    }

    fun BukkitRunnable.later (delay: Int): BukkitTask{
        return this.runTaskLater(plugin, delay.toLong())
    }
}