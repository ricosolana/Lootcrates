package com.crazicrafter1.lootcrates.listeners;

import com.crazicrafter1.lootcrates.LootCratesAPI;
import com.crazicrafter1.lootcrates.Main;
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

        if (Main.get().rev == -1 &&
                p.hasPermission("lootcrates.admin")) {
            Main.get().warn(p, "Unable to detect config revision");
            Main.get().warn(p, "Run " + ChatColor.UNDERLINE +"/crates rev <[int] | latest>" + ChatColor.RESET + ChatColor.YELLOW + " to fix this");
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e)
    {
        Player p = e.getPlayer();

        if (Main.get().openCrates.containsKey(p.getUniqueId())) {
            LootCratesAPI.closeCrate(p);
        }
    }
}
