package com.crazicrafter1.lootcrates.listeners;

import com.crazicrafter1.crutils.Version;
import com.crazicrafter1.lootcrates.LootCratesAPI;
import com.crazicrafter1.lootcrates.Main;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ListenerOnPlayerJoinQuit extends BaseListener {

    public ListenerOnPlayerJoinQuit(Main plugin) {
        super(plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        if (!p.hasPermission("lootcrates.admin"))
            return;

        if (plugin.rev == -1) {
            plugin.warn(p, ChatColor.GRAY + "Unable to detect config revision");
            TextComponent message = new TextComponent(ChatColor.GRAY + "To fix this, run: " + ChatColor.DARK_GRAY + "/crates rev <value> " + ChatColor.GOLD + ChatColor.BOLD + "[CLICK]");

            message.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/crates rev "));
            p.spigot().sendMessage(message);

        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e)
    {
        if (plugin.rev == -1)
            return;

        Player p = e.getPlayer();

        if (plugin.openCrates.containsKey(p.getUniqueId())) {
            LootCratesAPI.closeCrate(p);
        }
    }
}
