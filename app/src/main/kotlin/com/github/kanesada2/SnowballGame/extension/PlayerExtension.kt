package com.github.kanesada2.SnowballGame.extension

import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.util.Vector

fun Player.knockedBackedByProjectile(projectile: Projectile){
    if(this.gameMode == GameMode.CREATIVE) return
    val health: Double = this.health
    if (health <= 2) {
        this.health = health + 1
        this.damage(1.0)
    } else {
        this.damage(1.0)
        this.health = health
    }
    val knockbackVec: Vector = projectile.velocity.multiply(0.4)
    if (knockbackVec.getY() < 0.3) {
        knockbackVec.setY(0.3)
    }
    this.velocity = knockbackVec
}