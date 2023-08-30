package com.crazicrafter1.lootcrates.listeners;

import com.crazicrafter1.lootcrates.Lootcrates;
import com.crazicrafter1.lootcrates.LCMain;
import com.crazicrafter1.lootcrates.crate.CrateInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class ListenerOnInventoryClose extends BaseListener {

    public ListenerOnInventoryClose(LCMain plugin) {
        super(plugin);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e)
    {
        if (plugin.rev == -1)
            return;

        Player p = (Player)e.getPlayer();

        if (CrateInstance.CRATES.containsKey(p.getUniqueId())) {
            Lootcrates.stopCrate(p);
        }
    }

}
