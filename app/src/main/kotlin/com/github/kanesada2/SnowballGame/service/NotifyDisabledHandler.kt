package com.github.kanesada2.SnowballGame.service

import com.github.kanesada2.SnowballGame.SnowballGame
import org.bukkit.Bukkit
import java.io.File
import java.io.IOException
import java.io.PrintWriter
import java.nio.file.Files
import java.util.HashSet
import java.util.UUID

object NotifyDisabledHandler {
    lateinit var plugin: SnowballGame
        private set
    val notifyDisabled: HashSet<UUID?> = HashSet<UUID?>()

    fun init(plugin: SnowballGame) {
        this.plugin = plugin
    }

    fun loadNotifyDisabled() {
        try {
            notifyDisabled.clear()
            for (line in Files.readAllLines(File(plugin.dataFolder, "notify-disabled.txt").toPath())) {
                try {
                    notifyDisabled.add(UUID.fromString(line))
                } catch (e: IllegalArgumentException) {
                    Bukkit.getLogger().info("failed to load notify-disabled $line")
                }
            }
        } catch (e: IOException) {
            Bukkit.getLogger().info("Failed to load notify-disabled list")
            throw IOException(e)
        }
    }

    fun saveNotifyDisabled() {
        try {
            val notifyDisabledFile = File(plugin.dataFolder, "notify-disabled.txt")
            if (!notifyDisabledFile.exists()) notifyDisabledFile.createNewFile()
            PrintWriter(notifyDisabledFile, "UTF-8").use { writer ->
                writer.println("# The following players disabled SBG's notification for themselves")
                for (uuid in notifyDisabled) {
                    writer.println(uuid.toString())
                }
            }
        } catch (e: IOException) {
            Bukkit.getLogger().info("Failed to save notify-disabled list")
            throw IOException(e)
        }
    }

}