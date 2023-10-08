package com.crazicrafter1.lootcrates.listeners;

import com.crazicrafter1.lootcrates.LCMain;
import com.crazicrafter1.lootcrates.Lootcrates;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareInventoryResultEvent;

import java.util.Arrays;

public class ListenerDestroyCrate extends BaseListener {

    public ListenerDestroyCrate(LCMain plugin) {
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

    // checking for anvil is harder
//    @EventHandler
//    private void onPrepare(PrepareInventoryResultEvent e) {
//        //new PrepareAnvilEvent().se
//        //if (Arrays.stream(e.getInventory().getContents()).anyMatch(itemStack -> Lootcrates.getCrate(itemStack) != null).)
//    }

}
