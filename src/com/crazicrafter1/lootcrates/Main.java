package com.crazicrafter1.lootcrates;

import com.crazicrafter1.crutils.Metrics;
import com.crazicrafter1.crutils.Updater;
import com.crazicrafter1.crutils.Util;
import com.crazicrafter1.lootcrates.commands.CmdCrates;
import com.crazicrafter1.lootcrates.crate.ActiveCrate;
import com.crazicrafter1.lootcrates.crate.Crate;
import com.crazicrafter1.lootcrates.crate.LootSet;
import com.crazicrafter1.lootcrates.crate.loot.LootItem;
import com.crazicrafter1.lootcrates.crate.loot.LootItemCrate;
import com.crazicrafter1.lootcrates.crate.loot.LootItemQA;
import com.crazicrafter1.lootcrates.editor.loot.impl.EditLootItemMenu;
import com.crazicrafter1.lootcrates.listeners.*;
import com.crazicrafter1.lootcrates.tabs.TabCrates;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
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

    private FileConfiguration config = null;

    @Override
    public void onEnable() {

        Main.instance = this;

        /*
         * 1.17 assert
         */
        //if(ReflectionUtil.isOldVersion()) {
        //    error(
        //            "only MC 1.17+ is supported (Java 16)\n" +
        //            "please use LootCrates 3.1.4 and disable auto-update for legacy versions");

        //    getServer().getPluginManager().disablePlugin(this);
        //    return;
        //}

        supportQualityArmory = Bukkit.getPluginManager().isPluginEnabled("QualityArmory");

        // register serializable objects
        ConfigurationSerialization.registerClass(Data.class);
        ConfigurationSerialization.registerClass(LootSet.class);
        ConfigurationSerialization.registerClass(Crate.class);

        // api for easy
        LootCratesAPI.registerLoot(LootItemCrate.class/*, EditItemCrateMenu.class*/);
        LootCratesAPI.registerLoot(LootItem.class, EditLootItemMenu.class);
        LootCratesAPI.registerLoot(LootItemQA.class/*, EditItemQAMenu.class*/);

        // Save the config if it does not exist

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
        this.saveConfig();
    }

    public void saveDefaultConfig(boolean replace) {
        backupConfig();

        // If replacing, save
        // If file does not exist, save
        if (replace || !new File(this.getDataFolder(), "config.yml").exists())
            this.saveResource("config.yml", true);
    }

    int crashNext = 2;
    @Override
    public void reloadConfig() {
        if (crashNext-- == 0) {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        // Load file from jar if it doesnt exist
        saveDefaultConfig(false);

        if (this.config == null) {
            // Ready to parse
            this.config = new YamlConfiguration();
        }

        // Parse
        try {
            config.load(new File(this.getDataFolder(), "config.yml"));
            data = (Data) config.get("data");

            if (data == null) {
                error("Failed to serialize Main.get().data");
                // throw or something
                throw new NullDataException();
            }
            crashNext = 2;
        } catch (IOException | StackOverflowError e) {
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
        } catch (InvalidConfigurationException e) {
            error("Malformed config.yml (falling back ...)");
            e.printStackTrace();

            // then try loading again
            saveDefaultConfig(true);
            this.reloadConfig();
        } catch (NullDataException e) {
            error("Couldn't load Main.get().data (falling back ...)");

            e.printStackTrace();

            // then try loading again
            saveDefaultConfig(true);
            this.reloadConfig();
        }
    }

    public void backupConfig() {
        File configFile = new File(Main.get().getDataFolder(), "config.yml");
        File backupFile = new File(Main.get().getDataFolder(),"backup/broken" + System.currentTimeMillis() + "_config.yml");

        // create the backup path
        new File(Main.get().getDataFolder(),"backup/").mkdirs();

        try {
            // try to create the backup
            if (configFile.exists()) {
                backupFile.createNewFile();

                // copy the old to the new
                Util.copy(new FileInputStream(configFile), new FileOutputStream(backupFile));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveConfig() {
        // anytime config is saved, make a backup
        backupConfig();

        this.getConfig().set("data", data);

        try {
            this.getConfig().save(new File(this.getDataFolder(), "config.yml"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public FileConfiguration getConfig() {
        if (this.config == null) {
            this.reloadConfig();
        }
        return this.config;
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