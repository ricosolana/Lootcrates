package com.crazicrafter1.lootcrates.listeners;

import com.crazicrafter1.lootcrates.LCMain;
import com.crazicrafter1.lootcrates.crate.CrateInstance;
import org.bukkit.entity.Firework;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.FireworkExplodeEvent;

public class ListenerOnEntityDamageByEntity extends BaseListener {

    public ListenerOnEntityDamageByEntity(LCMain plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        //noinspection SuspiciousMethodCalls
        if (CrateInstance.crateFireworks.contains(e.getDamager())) {
            e.setCancelled(true);
        }
    }

    //@EventHandler
    //public void event(FireworkExplodeEvent e) {
    //    e.getEntity()
    //}
}
