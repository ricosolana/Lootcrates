package com.crazicrafter1.lootcrates.listeners;

import com.crazicrafter1.lootcrates.Main;
import org.bukkit.entity.Firework;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class ListenerOnEntityDamageByEntity extends BaseListener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {

        if (Main.crateFireWorks.contains(e.getDamager().getUniqueId())) {
            e.setCancelled(true);
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
