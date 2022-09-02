package com.crazicrafter1.lootcrates.listeners;

import com.crazicrafter1.lootcrates.LCMain;
import com.crazicrafter1.lootcrates.crate.CrateInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryDragEvent;

public class ListenerOnInventoryDrag extends BaseListener {

    public ListenerOnInventoryDrag(LCMain plugin) {
        super(plugin);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {
        if (plugin.rev == -1)
            return;

        Player p = (Player)e.getWhoClicked();
        if (CrateInstance.CRATES.containsKey(p.getUniqueId())) {
            e.setCancelled(true);
        } /*else {
            if (LootCratesAPI.extractCrateFromItem(e.getCursor()) != null)
                // Java 16+
                //switch (e.getInventory().getType()) {
                //    case ANVIL, SMOKER, BREWING, FURNACE, CRAFTING, MERCHANT, WORKBENCH, ENCHANTING, GRINDSTONE, STONECUTTER, BLAST_FURNACE -> {
                //        e.setCancelled(true);
                //    }
                //}
                switch (e.getInventory().getType()) {
                    case ANVIL:
                    case SMOKER:
                    case BREWING:
                    case FURNACE:
                    case CRAFTING:
                    case MERCHANT:
                    case WORKBENCH:
                    case ENCHANTING:
                    case GRINDSTONE:
                    case STONECUTTER:
                    case BLAST_FURNACE:
                        e.setCancelled(true);
                        break;
                }
        }*/
    }

}
