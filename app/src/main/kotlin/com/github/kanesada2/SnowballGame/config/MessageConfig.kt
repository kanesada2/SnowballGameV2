package com.github.kanesada2.SnowballGame.config

import com.github.kanesada2.SnowballGame.service.NotifyDisabledHandler
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player


data class BroadcastMessage(
    val enabled: Boolean = true,
    val range: Int = 50,
    val message: String = ""
) {
    fun format(vararg replacements: Pair<String, String>): String {
        var result = message
        replacements.forEach { (placeholder, value) ->
            result = result.replace("[[${placeholder}]]", value)
        }


        // カラーコード変換
        return result
            .replace("[[BLACK]]", ChatColor.BLACK.toString())
            .replace("[[DARK_BLUE]]", ChatColor.DARK_BLUE.toString())
            .replace("[[DARK_GREEN]]", ChatColor.DARK_GREEN.toString())
            .replace("[[DARK_AQUA]]", ChatColor.DARK_AQUA.toString())
            .replace("[[DARK_RED]]", ChatColor.DARK_RED.toString())
            .replace("[[DARK_PURPLE]]", ChatColor.DARK_PURPLE.toString())
            .replace("[[GOLD]]", ChatColor.GOLD.toString())
            .replace("[[GRAY]]", ChatColor.GRAY.toString())
            .replace("[[DARK_GRAY]]", ChatColor.DARK_GRAY.toString())
            .replace("[[BLUE]]", ChatColor.BLUE.toString())
            .replace("[[GREEN]]", ChatColor.GREEN.toString())
            .replace("[[AQUA]]", ChatColor.AQUA.toString())
            .replace("[[RED]]", ChatColor.RED.toString())
            .replace("[[LIGHT_PURPLE]]", ChatColor.LIGHT_PURPLE.toString())
            .replace("[[YELLOW]]", ChatColor.YELLOW.toString())
            .replace("[[WHITE]]", ChatColor.WHITE.toString())
            .replace("[[BOLD]]", ChatColor.BOLD.toString())
            .replace("[[UNDERLINE]]", ChatColor.UNDERLINE.toString())
            .replace("[[ITALIC]]", ChatColor.ITALIC.toString())
            .replace("[[STRIKE]]", ChatColor.STRIKETHROUGH.toString())
            .replace("[[MAGIC]]", ChatColor.MAGIC.toString())
            .replace("[[RESET]]", ChatColor.RESET.toString())
    }
}

enum class MessageType(val configKey: String) {
    SWING("Swing"),
    BUNT("Bunt"),
    HIT("Hit"),
    TAG("Tag"),
    CATCH("Catch"),
    STRIKE("Strike"),
    REACH_BASE("Reach_Base"),
    TOUCH_BASE("Touch_Base"),
    STANDING_BASE("Standing_Base")
}

// Config Section
object MessageConfig : ConfigSection {
    private var messages: Map<MessageType, BroadcastMessage> = emptyMap()

    override fun load(config: FileConfiguration) {
        val section = config.getConfigurationSection("Broadcast") ?: return

        messages = MessageType.entries.associateWith { type ->
            val typeSection = section.getConfigurationSection(type.configKey)
            BroadcastMessage(
                enabled = typeSection?.getBoolean("Enabled") ?: true,
                range = typeSection?.getInt("Range") ?: 50,
                message = typeSection?.getString("Message") ?: ""
            )
        }
    }

    operator fun get(type: MessageType): BroadcastMessage =
        messages[type] ?: BroadcastMessage()

    // 便利メソッド: 範囲内のプレイヤーにブロードキャスト
    fun broadcast(
        type: MessageType,
        location: Location,
        vararg replacements: Pair<String, String>
    ) {
        val config = this[type]
        if (!config.enabled) return

        val receivers = ArrayList<Player>()
        val players = location.world?.players
        if (players != null) {
            for (player in players) {
                if (NotifyDisabledHandler.notifyDisabled.contains(player.uniqueId)) {
                    continue
                }
                receivers.add(player)
            }
        }
        val formatted = config.format(*replacements)
        receivers
            .filter { it.location.distance(location) <= config.range }
            .forEach { it.sendMessage(formatted) }
    }
}