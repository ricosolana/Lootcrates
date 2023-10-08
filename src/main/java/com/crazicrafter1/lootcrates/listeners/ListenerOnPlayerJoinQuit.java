package com.crazicrafter1.lootcrates.listeners;

import com.crazicrafter1.lootcrates.Lootcrates;
import com.crazicrafter1.lootcrates.LCMain;
import com.crazicrafter1.lootcrates.crate.CrateInstance;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ListenerOnPlayerJoinQuit extends BaseListener {

    public ListenerOnPlayerJoinQuit(LCMain plugin) {
        super(plugin);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e)
    {
        Player p = e.getPlayer();

        if (CrateInstance.CRATES.containsKey(p.getUniqueId())) {
            Lootcrates.stopCrate(p);
        }
    }
}
