package com.github.kanesada2.SnowballGame.service

import com.github.kanesada2.SnowballGame.SnowballGame
import com.github.kanesada2.SnowballGame.item.BallItem
import com.github.kanesada2.SnowballGame.item.BaseItem
import com.github.kanesada2.SnowballGame.item.BatItem
import com.github.kanesada2.SnowballGame.item.CoachItem
import com.github.kanesada2.SnowballGame.item.GloveItem
import com.github.kanesada2.SnowballGame.item.HasRecipe
import com.github.kanesada2.SnowballGame.item.UmpireItem
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey

object RecipeManager {
    private val recipes = mutableListOf<NamespacedKey>()

    fun registerAll(plugin: SnowballGame) {
        register(plugin, "BallHighest", BallItem.generate("Highest"))
        register(plugin, "BallHigher", BallItem.generate("Higher"))
        register(plugin, "BallNormal", BallItem.generate("Normal"))
        register(plugin, "BallLower", BallItem.generate("Lower"))
        register(plugin, "BallLowest", BallItem.generate("Lowest"))

        register(plugin, "Bat", BatItem.generate())
        register(plugin, "Glove", GloveItem.generate())
        register(plugin, "Coach", CoachItem.generate())
        register(plugin, "Umpire", UmpireItem.generate())
        register(plugin, "Base", BaseItem.generate())
    }

    fun unregisterAll() {
        recipes.forEach { Bukkit.removeRecipe(it) }
        recipes.clear()
    }

    fun register (plugin: SnowballGame, name: String, item: HasRecipe) {
        val key = NamespacedKey(plugin, name)
        Bukkit.addRecipe(item.recipe(key))
        recipes.add(key)
    }
}