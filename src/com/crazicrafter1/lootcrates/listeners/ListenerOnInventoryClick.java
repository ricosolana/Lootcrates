package com.crazicrafter1.lootcrates.listeners;

import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.lootcrates.crate.Crate;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Arrays;
import java.util.HashSet;

public class ListenerOnInventoryClick extends BaseListener {

    public ListenerOnInventoryClick(Main plugin) {
        super(plugin);
    }

    private static HashSet<String> preventTypes = new HashSet<>();
    static {
        preventTypes.addAll(Arrays.asList(
                "FURNACE",
                "WORKBENCH",
                "SMOKER",
                "BLAST_FURNACE"
                ));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent e)
    {
        Player p = (Player) e.getWhoClicked();

        // If player is opening crate
        if (Main.openCrates.containsKey(p.getUniqueId()))
        {
            Main.openCrates.get(p.getUniqueId()).onInventoryClick(e);
        } else {
            /*
                TODO
                somehow prevent crates from being used as fuel or crafting recipes
             */

            //Main.getInstance().debug("hotbarbutton: " + e.getHotbarButton());
            //Main.getInstance().debug("slot: " + e.getSlot());
//
            //if (e.getClickedInventory() instanceof PlayerInventory)
            //    return;
//
            //Inventory clicked = e.getClickedInventory();
//
            //if (clicked == null || !preventTypes.contains(clicked.getType().name())) return;
//
            //ItemStack itemStack = e.getCurrentItem();
//
            //if (itemStack == null) {
//
            //    int hotbar = e.getHotbarButton();
            //    if (hotbar != -1)
            //        itemStack = p.getInventory().getItem(hotbar);
//
            //}
//
//
//
            //// If an item was PLACED INTO
            //if (itemStack != null && itemStack.getType() != Material.AIR) {
            //    // test that item
            //    if (Crate.matchCrate(itemStack) != null) {
            //        e.setCancelled(true);
            //    }
            //}

        }

    }

}
