package com.crazicrafter1.lootcrates.listeners;

import com.crazicrafter1.lootcrates.LCMain;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;

public class CraftingListener extends BaseListener {

    public CraftingListener(LCMain plugin) {
        super(plugin);
    }

    @EventHandler
    private void onInventory(InventoryEvent e) {
        if (e != null) {

        }
    }

    @EventHandler
    private void onCrafting(PrepareItemCraftEvent e) {
        //Bukkit.addRecipe()
        if (e != null) {

        }
    }

    @EventHandler
    private void onCraft(CraftItemEvent e) {
        if (e != null) {

        }
    }

}
