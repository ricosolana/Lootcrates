package com.crazicrafter1.lootcrates.listeners;

import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.lootcrates.crate.Crate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class ListenerOnDeath extends BaseListener {

    // get the handle first
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onDeath(PlayerDeathEvent e) {
        if (true) return;
        Player p = e.getEntity();
        if (Main.openCrates.containsKey(p.getUniqueId())) {
            //plugin.openCrates.get(p.getUniqueId()).giveAndCancel();
            Main.getInstance().debug("onDeath()");
            Crate.closeCrate(p);
        }
    }



}
