package com.crazicrafter1.lootcrates.listeners;

import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.lootcrates.crate.CrateInstance;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class ListenerOnEntityDamageByEntity extends BaseListener {

    public ListenerOnEntityDamageByEntity(Main plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        // TODO remove rev
        if (plugin.rev == -1)
            return;

        if (CrateInstance.crateFireworks.contains(e.getDamager().getUniqueId())) {
            e.setCancelled(true);
        }
    }
}
