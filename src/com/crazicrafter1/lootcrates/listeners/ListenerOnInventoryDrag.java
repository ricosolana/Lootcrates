package com.crazicrafter1.lootcrates.listeners;

import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.lootcrates.crate.Crate;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryDragEvent;

public class ListenerOnInventoryDrag extends BaseListener {

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {
        Player p = (Player)e.getWhoClicked();
        if (Main.get().openCrates.containsKey(p.getUniqueId())) {
            e.setCancelled(true);
        } else {
            if (Crate.crateByItem(e.getCursor()) != null)
                switch (e.getInventory().getType()) {
                    case ANVIL, SMOKER, BREWING, FURNACE, CRAFTING, MERCHANT, WORKBENCH, ENCHANTING, GRINDSTONE, STONECUTTER, BLAST_FURNACE -> {
                        e.setCancelled(true);
                    }
                }
        }
    }

}
