package com.crazicrafter1.lootcrates.listeners;

import com.crazicrafter1.lootcrates.Main;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

public class BaseListener implements Listener {

    protected static Main plugin = Main.get();

    public BaseListener(Main plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

}
