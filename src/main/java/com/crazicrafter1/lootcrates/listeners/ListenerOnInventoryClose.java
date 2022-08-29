package com.crazicrafter1.lootcrates.listeners;

import com.crazicrafter1.lootcrates.LootcratesAPI;
import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.lootcrates.crate.CrateInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class ListenerOnInventoryClose extends BaseListener {

    public ListenerOnInventoryClose(Main plugin) {
        super(plugin);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e)
    {
        if (plugin.rev == -1)
            return;

        Player p = (Player)e.getPlayer();

        if (CrateInstance.CRATES.containsKey(p.getUniqueId())) {
            LootcratesAPI.endDisplayCrateMenu(p);
        }
    }

}
