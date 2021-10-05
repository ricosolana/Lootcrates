package com.crazicrafter1.lootcrates.listeners;

import com.crazicrafter1.lootcrates.LootCratesAPI;
import com.crazicrafter1.lootcrates.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class ListenerOnInventoryClose extends BaseListener {

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e)
    {
        Player p = (Player)e.getPlayer();

        if (Main.get().openCrates.containsKey(p.getUniqueId())) {
            //plugin.openCrates.get(p.getUniqueId()).onInventoryClose(e);
            //plugin.openCrates.get(p.getUniqueId()).giveAndCancel();
            Main.get().debug("onInventoryClose()");
            LootCratesAPI.closeCrate(p);
        }
    }

}
