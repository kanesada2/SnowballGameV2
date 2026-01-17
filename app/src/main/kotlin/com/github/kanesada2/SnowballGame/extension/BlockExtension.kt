package com.github.kanesada2.SnowballGame.extension

import org.bukkit.Location
import org.bukkit.block.Block

fun Block.getCenterLocation(): Location {
    return  this.location.add(0.5, 0.5, 0.5)
}