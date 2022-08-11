package com.crazicrafter1.lootcrates.listeners;

import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.lootcrates.crate.CrateInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ListenerOnInventoryClick extends BaseListener {

    public ListenerOnInventoryClick(Main plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent e)
    {
        if (plugin.rev == -1)
            return;

        Player p = (Player) e.getWhoClicked();

        // If player is opening crate
        if (CrateInstance.CRATES.containsKey(p.getUniqueId())) {
            CrateInstance.CRATES.get(p.getUniqueId()).onInventoryClick(e);
        }/* else if (e.getClickedInventory() != null) {
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
