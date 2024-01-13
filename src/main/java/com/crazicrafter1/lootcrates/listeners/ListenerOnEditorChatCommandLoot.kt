package com.crazicrafter1.lootcrates.listeners

import com.crazicrafter1.crutils.Pair
import com.crazicrafter1.crutils.ui.AbstractMenu
import com.crazicrafter1.lootcrates.LCMain
import com.crazicrafter1.lootcrates.crate.loot.LootCommand
import org.bukkit.*
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.scheduler.BukkitRunnable
import java.util.*

class ListenerOnEditorChatCommandLoot(plugin: LCMain?) : BaseListener(plugin) {
    @EventHandler(priority = EventPriority.HIGHEST)
    private fun onAddCommand(event: AsyncPlayerChatEvent) {
        if (event.isAsynchronous) {
            // when player chats (expected)
            val command = awaitingCommands.remove(event.player.uniqueId)
            if (command != null) {
                // cancel send, this is a command input for LOOT
                event.isCancelled = true
                command.first.commands!!.add(event.message)
                event.player.sendMessage(ChatColor.GREEN.toString() + "Added command to CommandLoot!")
                event.player.sendMessage(ChatColor.GREEN.toString() + "Reopening...")

                // This runs later for small user delay, and to avoid async shenanigans
                object : BukkitRunnable() {
                    override fun run() {
                        // TODO is this check useless
                        // returns to menu
                        if (event.player.isOnline) command.second.open(event.player)
                    }
                }.runTaskLater(BaseListener.Companion.plugin!!, 30) // 1.5s
            }
        } else {
            // ignore / unexpected
        }
    }

    @EventHandler
    private fun onPlayerQuit(e: PlayerQuitEvent) {
        awaitingCommands.remove(e.player.uniqueId)
    }

    companion object {
        var awaitingCommands = Collections.synchronizedMap(HashMap<UUID, Pair<LootCommand, AbstractMenu.Builder>>())
    }
}
