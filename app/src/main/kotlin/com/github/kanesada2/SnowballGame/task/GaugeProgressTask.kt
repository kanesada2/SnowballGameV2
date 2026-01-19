package com.github.kanesada2.SnowballGame.task

import com.github.kanesada2.SnowballGame.Constants
import org.bukkit.boss.BossBar
import org.bukkit.scheduler.BukkitRunnable

class GaugeProgressTask(val bar: BossBar) : BukkitRunnable() {
    var ticks = 0
    override fun run() {
        ticks++

        when {
            // 1-20tick: 0→1に増加（1秒）
            ticks <= Constants.SwingGauge.CHARGE_TICKS -> {
                bar.progress = (ticks / Constants.SwingGauge.CHARGE_TICKS.toDouble()).coerceIn(0.0, 1.0)
            }
            // 21-24tick: 満タンで待機（0.2秒）
            ticks <= Constants.SwingGauge.FULL_HOLD_TICKS -> {
                bar.progress = 1.0
            }
            // 25tick: リセットして最初から
            else -> {
                ticks = 0
                bar.progress = Constants.SwingGauge.RESET_PROGRESS
            }
        }
    }
}