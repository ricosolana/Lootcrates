package com.crazicrafter1.lootcrates;

import com.crazicrafter1.crutils.Metrics;
import com.crazicrafter1.crutils.ReflectionUtil;
import com.crazicrafter1.crutils.Updater;
import com.crazicrafter1.crutils.Util;
import com.crazicrafter1.lootcrates.commands.CmdCrates;
import com.crazicrafter1.lootcrates.crate.ActiveCrate;
import com.crazicrafter1.lootcrates.crate.Crate;
import com.crazicrafter1.lootcrates.crate.LootGroup;
import com.crazicrafter1.lootcrates.crate.loot.LootItemCrate;
import com.crazicrafter1.lootcrates.crate.loot.LootItemQA;
import com.crazicrafter1.lootcrates.crate.loot.LootOrdinateItem;
import com.crazicrafter1.lootcrates.editor.loot.unique.EditItemCrateMenu;
import com.crazicrafter1.lootcrates.editor.loot.unique.EditItemQAMenu;
import com.crazicrafter1.lootcrates.editor.loot.unique.EditOrdinateItemMenu;
import com.crazicrafter1.lootcrates.listeners.*;
import com.crazicrafter1.lootcrates.tabs.TabCrates;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class Main extends JavaPlugin
{
    public final String prefix = ChatColor.translateAlternateColorCodes('&',
            "&f[&b&lLootCrates&r&f] ");

    /*
     * Runtime modifiable stuff
     */
    public HashMap<java.util.UUID, ActiveCrate> openCrates = new HashMap<>();
    public HashSet<UUID> crateFireworks = new HashSet<>();
    public boolean supportQualityArmory = false;

    private static Main instance;
    public static Main get() {
        return instance;
    }

    public Data data;

    @Override
    public void onEnable() {

        Main.instance = this;

        /*
         * 1.17 assert
         */
        if(ReflectionUtil.isOldVersion()) {
            error(
                    "only MC 1.17+ is supported (Java 16)\n" +
                    "please use LootCrates 3.1.4 and disable auto-update for legacy versions");

            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        supportQualityArmory = Bukkit.getPluginManager().isPluginEnabled("QualityArmory");

        // register serializable objects
        ConfigurationSerialization.registerClass(Data.class);
        ConfigurationSerialization.registerClass(LootGroup.class);
        ConfigurationSerialization.registerClass(Crate.class);

        // api for easy
        LootCratesAPI.registerLoot(LootItemCrate.class, EditItemCrateMenu.class);
        LootCratesAPI.registerLoot(LootOrdinateItem.class, EditOrdinateItemMenu.class);
        LootCratesAPI.registerLoot(LootItemQA.class, EditItemQAMenu.class);

        // Save the config if it does not exist
        saveDefaultConfig();

        // Overridden to load config and assign this.config
        this.reloadConfig();

        new Updater(this, "PeriodicSeizures", "LootCrates", data.update);

        /*
         * bStats metrics init
         */
        try {
            Metrics metrics = new Metrics(this, 10395);

            metrics.addCustomChart(new Metrics.SimplePie("updater", // what to record
                    () -> "" + data.update));

            metrics.addCustomChart(new Metrics.SimplePie("crates", // what to record
                    () -> "" + data.crates.size()));

            metrics.addCustomChart(new Metrics.SingleLineChart("opened", // what to record
                    () -> data.totalOpens));

        } catch (Exception e) {
            error("An error occurred while enabling metrics");
            if (data.debug)
                e.printStackTrace();
        }

        /*
         * Command init
         */
        new CmdCrates();
        new TabCrates();

        /*
         * Listener init
         */
        new ListenerOnEntityDamageByEntity();
        new ListenerOnInventoryClick();
        new ListenerOnInventoryClose();
        new ListenerOnInventoryDrag();
        new ListenerOnPlayerInteract();
        new ListenerOnPlayerQuit();
    }

    @Override
    public void onDisable() {
        // configs / backups
        File configFile = new File(Main.get().getDataFolder(), "config.yml");
        File backupFile = new File(Main.get().getDataFolder(),"backup/" + System.currentTimeMillis() + "_config.yml");

        // create the backup path
        new File(Main.get().getDataFolder(),"backup/").mkdirs();

        try {
            // try to create the backup
            backupFile.createNewFile();

            // copy the old to the new
            Util.copy(new FileInputStream(configFile), new FileOutputStream(backupFile));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Main.get().saveConfig();
    }

    @Override
    public void reloadConfig() {
        try {
            super.reloadConfig();
            data = (Data) getConfig().get("data");
        } catch (Exception e) {
            error("Couldn't load config (you modified it, didn't you?!?)");
            e.printStackTrace();
        }
    }

    @Override
    public void saveConfig() {
        this.getConfig().set("data", data);
        super.saveConfig();
    }

    public void info(String s) {
        Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.DARK_GRAY + s);
    }

    public void important(String s) {
        Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.DARK_PURPLE + s);
    }

    public void warn(String s) {
        Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.RED + s);
    }

    public void error(String s) {
        Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.DARK_RED + s);
    }

    public void debug(String s) {
        if (data.debug)
            Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.GOLD + s);
    }

}