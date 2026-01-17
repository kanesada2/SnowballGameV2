package com.github.kanesada2.SnowballGame.service

import com.github.kanesada2.SnowballGame.config.BounceConfig
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Projectile
import org.bukkit.util.Vector
import kotlin.math.abs

object BallHitFaceResolver {
    fun resolve(ball: Projectile, hitBlock: Block): BlockFace {
        val hitLoc = ball.location
        val velocity = ball.velocity
        // 薄いブロック
        if (BounceConfig.isAlwaysTop(hitBlock)) {
            return BlockFace.UP
        }

        var hitFace = hitBlock.getFace(hitLoc.block)
        // 都合の悪いhitFaceの場合は取り直す
        if (hitFace == null || hitFace == BlockFace.SELF || hitFace.toString().contains("_")) {
            hitFace = resolveReasonable(hitBlock, velocity)
        }
        return hitFace
    }

    private fun resolveReasonable(hitBlock: Block, velocity: Vector): BlockFace {
        // 反転させ、各成分ごとに隣をチェック。空いてるブロックの方向に跳ね返らせる
        val reversed = velocity.clone().normalize().multiply(-1)

        // 各成分の絶対値が大きい順にチェック
        val candidates = listOf(
            Triple(abs(reversed.x), if (reversed.x > 0) BlockFace.EAST else BlockFace.WEST, reversed.x),
            Triple(abs(reversed.y), if (reversed.y > 0) BlockFace.UP else BlockFace.DOWN, reversed.y),
            Triple(abs(reversed.z), if (reversed.z > 0) BlockFace.SOUTH else BlockFace.NORTH, reversed.z)
        ).sortedByDescending { it.first }

        for ((_, face, _) in candidates) {
            val adjacent = hitBlock.getRelative(face)
            if (BounceConfig.isPassthrough(adjacent)) {
                return face
            }
        }

        // 塞がっているなら最大の成分の方向を返す
        return candidates.first().second
    }


    private fun shouldSkip(block: Block, original: Block): Boolean {
        return BounceConfig.isPassthrough(block) || block.isLiquid || block == original
    }
}