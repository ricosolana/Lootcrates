package com.crazicrafter1.lootcrates.listeners;

import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.lootcrates.crate.Crate;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;

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
                ""
                //InventoryType.I
                ));
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e)
    {
        Player p = (Player) e.getView().getPlayer();

        if (Main.openCrates.containsKey(p.getUniqueId()))
        {
            Main.openCrates.get(p.getUniqueId()).onInventoryClick(e);
        } else {
            String invName = e.getInventory().getType().name();

            if (preventTypes.contains(invName)) {
                e.setCancelled(true);
            }
        }

    }

}
