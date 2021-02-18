package com.crazicrafter1.lootcrates.listeners;

import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.lootcrates.crate.Crate;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;

public class ListenerOnPlayerQuit extends BaseListener {

    public ListenerOnPlayerQuit(Main plugin) {
        super(plugin);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e)
    {
        Player p = e.getPlayer();

        if (plugin.openCrates.containsKey(p.getUniqueId())) {
            //plugin.openCrates.get(p.getUniqueId()).giveAndCancel(); //.onPlayerQuit(e);
            Crate.closeCrate(p);
            //plugin.openCrates.remove(p.getUniqueId());
        }

    }
}
