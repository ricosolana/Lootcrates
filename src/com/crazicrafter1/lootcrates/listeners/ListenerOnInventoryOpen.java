package com.crazicrafter1.lootcrates.listeners;

import com.crazicrafter1.lootcrates.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryOpenEvent;

public class ListenerOnInventoryOpen extends BaseListener {

    public ListenerOnInventoryOpen(Main plugin) {
        super(plugin);
    }

    @EventHandler
    void onInventoryOpen(InventoryOpenEvent e) {
        Player p = (Player) e.getPlayer();

        Main.get().info("InventoryOpenEvent: " +
                e.getInventory().getType().name());

        //Main.get().info(e.getInventory().getType().name());

        // cancel enderchest event if enderchest is opened

    }
}
