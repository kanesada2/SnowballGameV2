package com.github.kanesada2.SnowballGame.extension

import com.github.kanesada2.SnowballGame.entity.Ball
import org.bukkit.Bukkit
import org.bukkit.Server
import org.bukkit.entity.Snowball


fun Server.deleteBalls() {
    worlds.forEach {
        val balls = it.getEntitiesByClass(Snowball::class.java)
        Bukkit.getLogger().info("[SnowballGame] Deleting Balls in " + it.name + "...")
        balls.forEach{ snowball ->
            Ball.from(snowball)?.projectile?.remove()
        }
    }
}