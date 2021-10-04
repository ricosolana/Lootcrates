package com.crazicrafter1.lootcrates.listeners;

import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.lootcrates.crate.Crate;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ListenerOnInventoryClick extends BaseListener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent e)
    {
        Player p = (Player) e.getWhoClicked();

        // If player is opening crate
        if (Main.get().openCrates.containsKey(p.getUniqueId()))
        {
            Main.get().openCrates.get(p.getUniqueId()).onInventoryClick(e);
        } else if (e.getClickedInventory() != null) {
            if (Crate.crateByItem(e.getCursor()) != null)
                switch (e.getClickedInventory().getType()) {
                    case ANVIL, SMOKER, BREWING, FURNACE, CRAFTING, MERCHANT, WORKBENCH, ENCHANTING, GRINDSTONE, STONECUTTER, BLAST_FURNACE -> {
                        e.setCancelled(true);
                    }
                }
        } //else if (e.getCurrentItem())

    }

}
