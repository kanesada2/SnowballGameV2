package com.github.kanesada2.SnowballGame

import com.github.kanesada2.SnowballGame.config.BallConfig
import com.github.kanesada2.SnowballGame.config.CoachConfig
import com.github.kanesada2.SnowballGame.config.ConfigLoader
import com.github.kanesada2.SnowballGame.entity.Coach
import com.github.kanesada2.SnowballGame.entity.Knocker
import com.github.kanesada2.SnowballGame.extension.BukkitRunnableExtension.later
import com.github.kanesada2.SnowballGame.extension.MetaKeys
import com.github.kanesada2.SnowballGame.extension.MetadatableExtension.hasMeta
import com.github.kanesada2.SnowballGame.extension.MetadatableExtension.setMeta
import com.github.kanesada2.SnowballGame.item.BallItem
import com.github.kanesada2.SnowballGame.item.BaseItem
import com.github.kanesada2.SnowballGame.item.BatItem
import com.github.kanesada2.SnowballGame.item.CoachItem
import com.github.kanesada2.SnowballGame.item.GloveItem
import com.github.kanesada2.SnowballGame.item.UmpireItem
import com.github.kanesada2.SnowballGame.service.NotifyDisabledHandler
import com.github.kanesada2.SnowballGame.task.PlayerCoolDownTask
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.command.*
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

enum class SBGPermission(val node: String) {
    RELOAD("Snowballgame.reload"),
    GET("Snowballgame.get"),
    PLEASE("Snowballgame.please"),
    SWEEP("Snowballgame.sweep"),
    MSG("Snowballgame.msg"),
    UPDATE("Snowballgame.update");

    fun has(sender: CommandSender): Boolean = sender.hasPermission(node)
}
enum class SBGCommand(val permission: SBGPermission) {
    RELOAD(SBGPermission.RELOAD),
    GET(SBGPermission.GET),
    PLEASE(SBGPermission.PLEASE),
    SWEEP(SBGPermission.SWEEP),
    MSG(SBGPermission.MSG),
    UPDATE(SBGPermission.UPDATE);

    fun hasPermission(sender: CommandSender): Boolean = permission.has(sender)

    companion object {
        fun fromString(input: String): SBGCommand? {
            return entries.firstOrNull { it.name.equals(input, ignoreCase = true) }
        }
    }
}

class SnowballGameCommandExecutor: CommandExecutor, TabCompleter {
    private val subCommands = listOf("reload", "get", "please", "sweep", "update")
    private val itemTypes = listOf("Ball", "Bat", "Glove", "Umpire", "Coach", "Base")
    private val ballTypes = listOf("Highest", "Higher", "Normal", "Lower", "Lowest")

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String?>
    ): List<String> {
        if (!command.name.equals("SnowballGame", ignoreCase = true)) {
            return emptyList()
        }
        val input = args.lastOrNull()?.lowercase() ?: ""
        return when (args.size) {
            1 -> subCommands.filter { it.startsWith(input, ignoreCase = true) }
            2 -> if (args[0].equals("get", ignoreCase = true)) {
                itemTypes.filter { it.startsWith(input, ignoreCase = true) }
            } else emptyList()
            3 -> if (args[1].equals("ball", ignoreCase = true)) {
                ballTypes.filter { it.startsWith(input, ignoreCase = true) }
            } else emptyList()
            else -> emptyList()
        }
    }

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String?>
    ): Boolean {
        if (!command.name.equals("SnowballGame", ignoreCase = true)) {
            return false
        }
        if(args.isEmpty()){
            showHelp(sender)
            return true
        }
        val sbgCommand = args[0]?.let { SBGCommand.fromString(it) } ?: run {
            sender.sendMessage("Unknown command. Please check /sbg")
            return false
        }

        if (!sbgCommand.hasPermission(sender)) {
            sender.sendMessage("You don't have the permission to use this command.")
            return false
        }
        return when (args[0]?.lowercase()) {
            "reload" -> handleReload(sender)
            "get" -> handleGet(sender, args)
            "please" -> handlePlease(sender)
            "sweep" -> handleSweep(sender)
            "msg" -> handleMsg(sender, args)
            "update" -> handleUpdate(sender)
            else -> {
                sender.sendMessage("Unknown command: ${args[0]}")
                false
            }
        }
    }

    private fun showHelp(sender: CommandSender) {
        sender.sendMessage(
            "/sbg ${ChatColor.YELLOW}Show all SnowballGame commands.",
            "/sbg reload ${ChatColor.YELLOW}Reload SnowballGame's config file",
            "/sbg get [Ball|Bat|Glove] <Highest|Higher|Normal|Lower|Lowest> ${ChatColor.YELLOW}Get SnowballGame's custom item.",
            "/sbg please ${ChatColor.YELLOW}Coach hit the ball for your fielding practice.",
            "/sbg sweep ${ChatColor.YELLOW}Clean up floating balls around you(in 3 blocks).",
            "/sbg msg [on|off] ${ChatColor.YELLOW} Enable|disable SBG's notification to you",
            "/sbg update ${ChatColor.YELLOW}Convert old version items in your inventory to the current format."
        )
    }

    private fun handleReload (sender: CommandSender): Boolean {
        if(sender !is ConsoleCommandSender) return false
        ConfigLoader.load()
        Bukkit.getLogger().info("SnowballGame Reloaded!")
        return true
    }
    private fun handlePlease (sender: CommandSender): Boolean {
        if (sender !is Player) {
            sender.sendMessage("Please send this command in game.")
            return false
        }
        if (sender.hasMeta(MetaKeys.PROHIBIT_CALLING_KNOCK)) {
            sender.sendMessage("Your coach can't hit the next ball so quickly.")
            return false
        }
        sender.inventory.firstNotNullOfOrNull(BallItem::from)?.let { ball->
            sender.getNearbyEntities(CoachConfig.range, 10.0, CoachConfig.range)
                .filterIsInstance<ArmorStand>()
                .firstNotNullOfOrNull(Coach::from)
                ?.let {coach ->
                    Knocker.from(coach)?.hitBall(ball.ballType!!, sender)
                }?:run{
                sender.sendMessage("You are too far from your coach to practice.")
                return false
            }
            // これをやりつつコーチがいなかったとき失敗させなきゃいけないのでネストが深い
            ball.item.amount -= 1
        }?: run {
            sender.sendMessage("You must have at least one ball to send this command.")
            return false
        }
        sender.setMeta(MetaKeys.PROHIBIT_CALLING_KNOCK, true)
        PlayerCoolDownTask(sender).later(BallConfig.coolTime)
        return true
    }
    private fun handleSweep (sender: CommandSender): Boolean {
        if (sender !is Player) {
            sender.sendMessage("Please send this command in game.")
            return false
        }
        val entities = sender.getNearbyEntities(3.0, 3.0, 3.0)
        var count = 0
        for (entity in entities) {
            if (entity is Snowball) {
                entity.remove()
                count++
            }
        }
        if (count == 0) {
            sender.sendMessage("No balls was found around you.")
            return false
        }
        sender.sendMessage("$count balls successfully cleaned up!")
        return true
    }
    private fun handleUpdate (sender: CommandSender): Boolean {
        if(sender !is Player){
            sender.sendMessage("Please send this command in game.")
            return false
        }
        val items = sender.inventory.filterNotNull().filter{it.itemMeta?.lore?.contains("SnowballGame Item") ?: false}
        if(items.isEmpty()){
            sender.sendMessage("You don't have any old version items.")
            return false
        }
        for (item in items) {
            val name = item.itemMeta?.displayName
            val converted =
            when (item.type) {
                Material.ARMOR_STAND -> CoachItem.generate(name).item
                Material.BOW -> BatItem.generate(name).item
                Material.LEATHER -> GloveItem.generate(name).item
                Material.QUARTZ_BLOCK -> UmpireItem.generate(name).item
                Material.QUARTZ_SLAB -> BaseItem.generate(name).item
                Material.SNOWBALL -> {
                    val repulsionLores = arrayOf(
                        "Highest-repulsion",
                        "Higher-repulsion",
                        "Lower-repulsion",
                        "Lowest-repulsion",
                    )
                    val type = repulsionLores.firstOrNull{ item.itemMeta?.lore?.contains(it) ?: false}?.replace("-repulsion", "") ?: "Normal"
                    BallItem.generate(type, name).item
                }
                else -> return false
            }
            sender.inventory.remove(item)
            sender.inventory.addItem(converted)
        }
        sender.sendMessage("Update in your inventory completed!")
        return true
    }
    private fun handleMsg (sender: CommandSender, args: Array<out String?>): Boolean {
        if(sender !is Player){
            sender.sendMessage("Please send this command in game.")
            return false
        }
        if (args[1].equals("off", ignoreCase = true)) {
            if(NotifyDisabledHandler.notifyDisabled.contains(sender.uniqueId)){
                sender.sendMessage("Your setting is already off.")
                return false
            }
            NotifyDisabledHandler.notifyDisabled.add(sender.uniqueId)
            sender.sendMessage("Disabled SBG's message to you.")
            return true
        }else if (args[1].equals("on", ignoreCase = true)) {
            if(!NotifyDisabledHandler.notifyDisabled.contains(sender.uniqueId)){
                sender.sendMessage("Your setting is already on.")
                return false
            }
            NotifyDisabledHandler.notifyDisabled.remove(sender.uniqueId)
            sender.sendMessage("You can enjoy sounds of baseball again!")
            return true
        }
        sender.sendMessage("Wrong command. please check /sbg")
        return false
    }
    private fun handleGet (sender: CommandSender, args: Array<out String?>): Boolean {
        if(sender !is Player){
            sender.sendMessage("Please send this command in game.")
            return false
        }
        if(args.size < 2){
            sender.sendMessage("Please choice the type of item you want.")
            return false
        }
        if(args.size == 3 && !args[1].equals("ball", ignoreCase = true)) {
            sender.sendMessage("You can't choice the type of such a item.")
            return false
        }
        if(!itemTypes.any{ it.equals(args[1], ignoreCase = true) }){
            sender.sendMessage("SnowballGame can't provide such a item.")
            return false
        }
        var item: ItemStack? = null
        if(args[1].equals("ball", ignoreCase = true)) {
            val type = if(args.size >= 3) args[2] else "Normal"
            if(!ballTypes.contains(type)) {
                sender.sendMessage("SnowballGame can't provide such a item.")
                return false
            }
            item = type?.let { BallItem.generate(it) }?.item
        }
        if(args[1].equals("bat", ignoreCase = true)) {
            item = BatItem.generate().item
        }
        if(args[1].equals("glove", ignoreCase = true)) {
            item = GloveItem.generate().item
        }
        if(args[1].equals("base", ignoreCase = true)) {
            item = BaseItem.generate().item
        }
        if(args[1].equals("umpire", ignoreCase = true)) {
            item = UmpireItem.generate().item
        }
        if(args[1].equals("coach", ignoreCase = true)) {
            item = CoachItem.generate().item
        }
        val inv: Inventory = sender.inventory
        if (inv.containsAtLeast(item, 1) || inv.firstEmpty() != -1) {
            inv.addItem(item)
        } else {
            item?.let { sender.world.dropItem(sender.location, it) }
        }
        sender.sendMessage("You got a SnowballGame's item!")
        return true
    }

}