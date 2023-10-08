package com.crazicrafter1.lootcrates.listeners;

import com.crazicrafter1.lootcrates.LCMain;
import com.crazicrafter1.lootcrates.crate.CrateInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ListenerCrateInteract extends BaseListener {

    public ListenerCrateInteract(LCMain plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent e)
    {
        Player p = (Player) e.getWhoClicked();

        // If player is opening crate
        CrateInstance instance = CrateInstance.CRATES.get(p.getUniqueId());
        if (instance != null) {
            instance.onInventoryClick(e);
        }
        //else if (e.getClick().isCreativeAction()
        //    && Lootcrates.getCrate(e.getCurrentItem()) != null) {
        //    // cancel
        //    e.setCancelled(true);
        //}


        /* else if (e.getClickedInventory() != null) {
            if (LootCratesAPI.extractCrateFromItem(e.getCursor()) != null) {

                Main.get().info(e.getClickedInventory().getType().name());

                switch (e.getClickedInventory().getType()) {
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
            }
        }*/

    }

}
