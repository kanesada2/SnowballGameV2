package com.github.kanesada2.SnowballGame.service

import com.github.kanesada2.SnowballGame.Constants
import com.github.kanesada2.SnowballGame.config.BounceConfig
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.block.BlockFace

object BallHitLocationCorrector {
    private fun needsCorrection(hitLocation: Location, hitBlock: Block, hitFace: BlockFace): Boolean {
        val adjacentBlock = hitBlock//.getRelative(hitFace)

        if (BounceConfig.isPassthrough(adjacentBlock)) return false
        if (BounceConfig.isAlwaysTop(adjacentBlock)) return false

        val box = adjacentBlock.boundingBox

        // hitLocationがadjacentBlockのBoundingBox内にめり込んでるか
        return when (hitFace) {
            BlockFace.UP -> hitLocation.y > box.minY
            BlockFace.DOWN -> hitLocation.y < box.maxY
            BlockFace.WEST -> hitLocation.x > box.minX
            BlockFace.EAST -> hitLocation.x < box.maxX
            BlockFace.NORTH -> hitLocation.z > box.minZ
            BlockFace.SOUTH -> hitLocation.z < box.maxZ
            else -> false
        }
    }

    fun correctHitLocationIfNeeded(hitLocation: Location, hitFace: BlockFace, hitBlock: Block): Location {
        if(!needsCorrection(hitLocation, hitBlock, hitFace)) return hitLocation

        val targetBlock = generateSequence(hitBlock) { it.getRelative(hitFace) }
            .take(Constants.Misc.MAX_CORRECTION_SEARCH_DEPTH)
            .firstOrNull { BounceConfig.isPassthrough(it.getRelative(hitFace)) || BounceConfig.isAlwaysTop(it.getRelative(hitFace)) }
            ?: return hitLocation

        val adjacentBlock = targetBlock.getRelative(hitFace)

        when (hitFace) {
            BlockFace.UP -> hitLocation.y = adjacentBlock.y.toDouble()
            BlockFace.DOWN -> hitLocation.y = targetBlock.y.toDouble()
            BlockFace.WEST -> hitLocation.x = adjacentBlock.x.toDouble()
            BlockFace.EAST -> hitLocation.x = targetBlock.x.toDouble()
            BlockFace.NORTH -> hitLocation.z = adjacentBlock.z.toDouble()
            BlockFace.SOUTH -> hitLocation.z = targetBlock.z.toDouble()
            else -> return hitLocation
        }
        return hitLocation
    }
}