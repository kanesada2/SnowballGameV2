package com.github.kanesada2.SnowballGame

import com.github.kanesada2.SnowballGame.config.ConfigLoader
import com.github.kanesada2.SnowballGame.extension.BukkitRunnableExtension
import com.github.kanesada2.SnowballGame.extension.MetadatableExtension
import com.github.kanesada2.SnowballGame.extension.deleteBalls
import com.github.kanesada2.SnowballGame.service.NotifyDisabledHandler
import com.github.kanesada2.SnowballGame.service.RecipeManager
import org.bukkit.plugin.java.JavaPlugin


class SnowballGame : JavaPlugin() {
    override fun onEnable() {
        ConfigLoader.init(this)
        ConfigLoader.load()
        PersistentDataKeys.init(this)
        RecipeManager.registerAll(this)
        MetadatableExtension.init(this)
        BukkitRunnableExtension.init(this)
        NotifyDisabledHandler.init(this)
        NotifyDisabledHandler.loadNotifyDisabled()
        server.pluginManager.registerEvents(SnowballGameListener(), this)
        getCommand("SnowballGame")?.setExecutor(SnowballGameCommandExecutor())
        logger.info("SnowballGame is enabled!")
    }

    override fun onDisable() {
        RecipeManager.unregisterAll()
        NotifyDisabledHandler.saveNotifyDisabled()
        server.deleteBalls()
        logger.info("SnowballGame is disabled")
    }
}