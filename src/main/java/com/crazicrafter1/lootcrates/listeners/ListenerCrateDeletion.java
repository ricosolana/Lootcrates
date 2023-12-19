package com.crazicrafter1.lootcrates.listeners;

import com.crazicrafter1.lootcrates.LCMain;
import com.crazicrafter1.lootcrates.Lootcrates;
import com.crazicrafter1.lootcrates.crate.CrateInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public class ListenerCrateDeletion extends BaseListener {

    public ListenerCrateDeletion(LCMain plugin) {
        super(plugin);
    }

    @EventHandler
    private void onFurnaceBurn(FurnaceBurnEvent e) {
        // creative inventory is partly client side (making middle clicks not detectable)
        //if (e.getClick().isCreativeAction()
        //       && Lootcrates.getCrate(e.getCurrentItem()) != null) {
        //   // cancel
        //   e.setCancelled(true);
        //}
        if (Lootcrates.getCrate(e.getFuel()) != null) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    private void onInventoryDrag(InventoryDragEvent e) {
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

    // checking for anvil is harder
//    @EventHandler
//    private void onPrepare(PrepareInventoryResultEvent e) {
//        //new PrepareAnvilEvent().se
//        //if (Arrays.stream(e.getInventory().getContents()).anyMatch(itemStack -> Lootcrates.getCrate(itemStack) != null).)
//    }

}
