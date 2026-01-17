package com.github.kanesada2.SnowballGame.extension

import org.bukkit.NamespacedKey
import org.bukkit.entity.Entity
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Vector
import java.nio.ByteBuffer

fun PersistentDataContainer.setVector(key: NamespacedKey, vec: Vector) {
    val buffer = ByteBuffer.allocate(24)  // 8bytes * 3
    buffer.putDouble(vec.x)
    buffer.putDouble(vec.y)
    buffer.putDouble(vec.z)
    set(key, PersistentDataType.BYTE_ARRAY, buffer.array())
}

fun PersistentDataContainer.getVector(key: NamespacedKey): Vector? {
    val bytes = get(key, PersistentDataType.BYTE_ARRAY) ?: return null
    val buffer = ByteBuffer.wrap(bytes)
    return Vector(buffer.getDouble(), buffer.getDouble(), buffer.getDouble())
}

fun Entity.hasPdc(key: NamespacedKey): Boolean {
    return persistentDataContainer.keys.contains(key)
}

fun <T : Any> Entity.getPdc(key: NamespacedKey, type: PersistentDataType<*, T>): T? {
    return persistentDataContainer.get(key, type)
}

fun Entity.getPdcVector(key: NamespacedKey): Vector? {
    return persistentDataContainer.getVector(key)
}

fun <T : Any> Entity.setPdc(key: NamespacedKey, type: PersistentDataType<*, T>, value: T) {
    persistentDataContainer.set(key, type, value)
}

fun Entity.setPdcVector(key: NamespacedKey, vector: Vector) {
    persistentDataContainer.setVector(key, vector)
}

fun Entity.removePdc(key: NamespacedKey) {
    persistentDataContainer.remove(key)
}

fun ItemStack.hasPdc(key: NamespacedKey): Boolean {
    return itemMeta?.persistentDataContainer?.keys?.contains(key) ?: false
}

fun <T : Any> ItemStack.getPdc(key: NamespacedKey, type: PersistentDataType<*, T>): T? {
    return itemMeta?.persistentDataContainer?.get(key, type)
}

fun ItemStack.getPdcVector(key: NamespacedKey): Vector? {
    return itemMeta?.persistentDataContainer?.getVector(key)
}

// setはmetaを適用し直す必要があるのでやらない

fun ItemStack.removePdc(key: NamespacedKey) {
    itemMeta = itemMeta?.apply {
        persistentDataContainer.remove(key)
    }
}