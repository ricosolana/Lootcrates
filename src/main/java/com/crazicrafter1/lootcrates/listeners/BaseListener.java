package com.crazicrafter1.lootcrates.listeners;

import com.crazicrafter1.lootcrates.LCMain;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

public class BaseListener implements Listener {
    protected static LCMain plugin = LCMain.get();

    public BaseListener(LCMain plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
}
