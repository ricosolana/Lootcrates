package com.crazicrafter1.lootcrates.listeners;

import com.crazicrafter1.crutils.Pair;
import com.crazicrafter1.crutils.ui.AbstractMenu;
import com.crazicrafter1.lootcrates.LCMain;
import com.crazicrafter1.lootcrates.Lootcrates;
import com.crazicrafter1.lootcrates.crate.CrateInstance;
import com.crazicrafter1.lootcrates.crate.loot.LootCommand;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class ListenerOnEditorChatCommandLoot extends BaseListener {

    public static Map<UUID, Pair<LootCommand, AbstractMenu.Builder>> awaitingCommands = Collections.synchronizedMap(new HashMap<>());

    public ListenerOnEditorChatCommandLoot(LCMain plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onAddCommand(AsyncPlayerChatEvent event) {
        if (event.isAsynchronous()) {
            // when player chats (expected)

            Pair<LootCommand, AbstractMenu.Builder> command = awaitingCommands.remove(event.getPlayer().getUniqueId());
            if (command != null) {
                // cancel send, this is a command input for LOOT
                event.setCancelled(true);

                command.first.commands.add(event.getMessage());

                event.getPlayer().sendMessage(ChatColor.GREEN + "Added command to CommandLoot!");
                event.getPlayer().sendMessage(ChatColor.GREEN + "Reopening...");

                // This runs later for small user delay, and to avoid async shenanigans
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        // TODO is this check useless
                        // returns to menu
                        if (event.getPlayer().isOnline())
                            command.second.open(event.getPlayer());
                    }
                }.runTaskLater(plugin, 30); // 1.5s
            }
        } else {
            // ignore / unexpected
        }
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent e) {
        awaitingCommands.remove(e.getPlayer().getUniqueId());
    }

}
