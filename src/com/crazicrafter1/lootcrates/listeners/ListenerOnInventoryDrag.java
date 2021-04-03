package com.crazicrafter1.lootcrates.listeners;

import com.crazicrafter1.lootcrates.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryDragEvent;

public class ListenerOnInventoryDrag extends BaseListener {

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {
        Player p = (Player)e.getWhoClicked();
        if (Main.openCrates.containsKey(p.getUniqueId())) {
            e.setCancelled(true);
        }
    }

}
