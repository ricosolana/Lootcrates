package com.crazicrafter1.lootcrates.listeners;

import com.crazicrafter1.lootcrates.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryDragEvent;

public class ListenerOnInventoryDrag extends BaseListener {

    public ListenerOnInventoryDrag(Main plugin) {
        super(plugin);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {
        if (e.getView().getPlayer() instanceof Player) {
            Player p = (Player)e.getView().getPlayer();
            if (plugin.openCrates.containsKey(p.getUniqueId())) {
                plugin.openCrates.get(p.getUniqueId()).onInventoryDrag(e);
            }
        }
    }

}
