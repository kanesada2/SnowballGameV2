package com.github.kanesada2.SnowballGame.task

import org.bukkit.boss.BossBar
import org.bukkit.scheduler.BukkitRunnable

class GaugeProgressTask(val bar: BossBar) : BukkitRunnable() {
    var ticks = 0
    override fun run() {
        ticks++

        when {
            // 1-20tick: 0→1に増加（1秒）
            ticks <= 20 -> {
                bar.progress = (ticks / 20.0).coerceIn(0.0, 1.0)
            }
            // 21-24tick: 満タンで待機（0.2秒）
            ticks <= 24 -> {
                bar.progress = 1.0
            }
            // 25tick: リセットして最初から
            else -> {
                ticks = 0
                bar.progress = 0.05
            }
        }
    }
}