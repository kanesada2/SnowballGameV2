package com.github.kanesada2.SnowballGame.extension

import com.github.kanesada2.SnowballGame.SnowballGame
import org.bukkit.Location
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.metadata.Metadatable
import org.bukkit.util.Vector

class MetadataKey<T>(val name: String)
object MetaKeys {
    // Ball
    val SPIN_VECTOR = MetadataKey<Vector>("spinVector")
    val MOVING_TYPE = MetadataKey<String>("movingType")

    val IS_IN_FLIGHT = MetadataKey<Boolean>("isInFlight")
    val PREV_BOUND_LOCATION = MetadataKey<Location>("prevBoundLocation")
    val SAME_PLACE_COUNT = MetadataKey<Int>("samePlaceCount")

    // Player
    val PROHIBIT_THROWING = MetadataKey<Boolean>("prohibitThrowing")
    val PROHIBIT_SLIDING = MetadataKey<Boolean>("prohibitSliding")
    val PROHIBIT_CALLING_KNOCK = MetadataKey<Boolean>("prohibitCallingKnock")
    val SWING_STATE = MetadataKey<String>("swingState")
    val PROHIBIT_PROGRESS_SWING = MetadataKey<Boolean>("prohibitProgressSwing")

    // Dispenser
    val DISPENSING_ITEM = MetadataKey<ItemStack>("dispensingItem")
}

object MetadatableExtension {
    lateinit var plugin: SnowballGame
        private set

    fun init(plugin: SnowballGame) {
        this.plugin = plugin
    }

    fun Metadatable.hasMeta(key: MetadataKey<*>): Boolean {
        return hasMetadata(key.name);
    }

    // 拡張関数
    fun <T : Any> Metadatable.setMeta(key: MetadataKey<T>, value: T,) {
        setMetadata(key.name, FixedMetadataValue(plugin, value))
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> Metadatable.getMeta(key: MetadataKey<T>): T? {
        return getMetadata(key.name)
            .firstOrNull { it.owningPlugin == plugin }
            ?.value() as? T
    }

    fun <T> Metadatable.removeMeta(key: MetadataKey<T>) {
        removeMetadata(key.name, plugin)
    }

    fun <T : Any> Metadatable.updateMeta(key: MetadataKey<T>, value: T,) {
        if(hasMeta(key)){
            removeMeta(key)
        }
        setMeta(key, value)
    }
}