package com.crazicrafter1.lootcrates.listeners;

import com.crazicrafter1.lootcrates.Main;
import org.bukkit.plugin.java.JavaPlugin;

public class ListenerManager {
    
    protected static Main plugin = Main.getInstance();
    
    public static void init() {


        org.bukkit.Bukkit.getPluginManager().registerEvents(new ListenerOnChunkLoad(), plugin);
        org.bukkit.Bukkit.getPluginManager().registerEvents(new ListenerOnEntityDamageByEntity(), plugin);
        org.bukkit.Bukkit.getPluginManager().registerEvents(new ListenerOnInventoryClick(), plugin);
        org.bukkit.Bukkit.getPluginManager().registerEvents(new ListenerOnInventoryClose(), plugin);
        org.bukkit.Bukkit.getPluginManager().registerEvents(new ListenerOnInventoryDrag(), plugin);
        org.bukkit.Bukkit.getPluginManager().registerEvents(new ListenerOnPlayerInteract(), plugin);
        org.bukkit.Bukkit.getPluginManager().registerEvents(new ListenerOnPlayerQuit(), plugin);
        org.bukkit.Bukkit.getPluginManager().registerEvents(new ListenerOnArmorstandInteract(), plugin);

        //org.bukkit.Bukkit.getPluginManager().registerEvents(new ListenerOnAsyncPlayerChat(), plugin);
        
    }
    
}
