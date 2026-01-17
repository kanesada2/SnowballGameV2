package com.github.kanesada2.SnowballGame.entity

import com.github.kanesada2.SnowballGame.PersistentDataKeys
import com.github.kanesada2.SnowballGame.config.UmpireConfig
import com.github.kanesada2.SnowballGame.extension.BukkitRunnableExtension.repeat
import com.github.kanesada2.SnowballGame.extension.hasPdc
import com.github.kanesada2.SnowballGame.task.BallJudgeTask
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Projectile
import org.bukkit.util.Vector

@JvmInline
value class Umpire(val stand: ArmorStand) {
    companion object {
        fun from(stand: ArmorStand): Umpire? {
            if(!UmpireConfig.enabled) return null
            return if(stand.hasPdc(PersistentDataKeys.Umpire)) Umpire(stand) else null
        }
    }
    fun prepare(ball: Projectile){
        val maximumCorner = stand.location.add(Vector(0.5, UmpireConfig.top, 0.5))
        val minimumCorner = stand.location.add(Vector(-0.5, UmpireConfig.bottom, -0.5))
        BallJudgeTask(ball, stand, minimumCorner, maximumCorner).repeat(0, 1)
    }
}