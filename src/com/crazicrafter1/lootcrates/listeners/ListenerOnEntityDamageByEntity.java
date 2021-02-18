package com.crazicrafter1.lootcrates.listeners;

import com.crazicrafter1.lootcrates.Main;
import org.bukkit.entity.Firework;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class ListenerOnEntityDamageByEntity extends BaseListener {

    public ListenerOnEntityDamageByEntity(Main plugin) {
        super(plugin);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {

        if ((e.getDamager() instanceof Firework)) {
            if (plugin.crateFireWorks.contains(e.getDamager())) {
                e.setCancelled(true);
            }
        }
        /*
        if ((e.getDamager() instanceof Firework))
        {
            if (e.getEntity() instanceof Player)
            {
                Player p = (Player)e.getEntity();
                if (Main.openCrates.containsKey(p.getUniqueId())) {
                    e.setCancelled(true);
                }
            }
        }

         */
    }

}
