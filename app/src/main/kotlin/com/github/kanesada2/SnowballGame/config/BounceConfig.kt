package com.github.kanesada2.SnowballGame.config

import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.configuration.file.FileConfiguration

data class BlockTypeConfig(
    val repulsion: Double,
    val alwaysTop: Boolean = false,
    val blocks: List<Material>
)

// 全体のConfig Section
object BounceConfig : ConfigSection {
    private var types: Map<String, BlockTypeConfig> = emptyMap()
    private var blockToType: Map<Material, Pair<String, BlockTypeConfig>> = emptyMap()

    override fun load(config: FileConfiguration) {
        val typeList = config.getStringList("Type")
        types = buildMap {
            typeList.forEach { type ->
                val section = config.getConfigurationSection(type)
                put(type, BlockTypeConfig(
                    repulsion = section?.getDouble("Repulsion", 1.0)?: 1.0,
                    alwaysTop = section?.getBoolean("AlwaysTop", false) ?: false,
                    blocks = section?.getStringList("List")?.map {
                        Material.valueOf(it.uppercase())
                    }?: emptyList()
                ))
            }
        }
        // 逆引きマップを構築
        blockToType = buildMap {
            types.forEach { (name, config) ->
                config.blocks.forEach { material ->
                    put(material, name to config)
                }
            }
        }
    }

    operator fun get(typeName: String): BlockTypeConfig? = types[typeName]

    // ブロックからタイプを逆引き
    fun getTypeFor(block: Block): Pair<String, BlockTypeConfig>? {
        val material = block.type
        return types.entries.firstOrNull { (_, config) ->
            material in config.blocks
        }?.toPair()
    }

    fun getRepulsion(block: Block): Double {
        return getTypeFor(block)?.second?.repulsion ?: 1.0
    }

    fun isAlwaysTop(block: Block): Boolean {
        return getTypeFor(block)?.second?.alwaysTop ?: false
    }

    fun isPassthrough(block: Block): Boolean {
        return block.isPassable || block.isLiquid || getTypeFor(block)?.second?.repulsion == 0.0
    }
}