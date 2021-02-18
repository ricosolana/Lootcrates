package com.crazicrafter1.lootcrates.listeners;

import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.lootcrates.crate.Crate;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerPortalEvent;

public class ListenerOnPortal extends BaseListener {

    public ListenerOnPortal(Main plugin) {
        super(plugin);
    }

    @EventHandler
    public void onPortalEnter(PlayerPortalEvent e) {
        Player p = e.getPlayer();
        if (plugin.openCrates.containsKey(p.getUniqueId())) {
            //plugin.openCrates.get(p.getUniqueId()).giveAndCancel();
            Crate.closeCrate(p);
        }
    }

}
