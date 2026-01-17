package com.github.kanesada2.SnowballGame.item

import org.bukkit.NamespacedKey
import org.bukkit.inventory.Recipe

interface HasRecipe {
    fun recipe(key: NamespacedKey): Recipe
}