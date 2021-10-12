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
    public HashMap<UUID, ActiveCrate> openCrates = new HashMap<>();
    public HashSet<UUID> crateFireworks = new HashSet<>();
    public boolean supportQualityArmory = false;

    public Data data;
    private FileConfiguration config = null;
    private final File configFile = new File(getDataFolder(), "config.yml");

    private static Main instance;
    public static Main get() {
        return instance;
    }

    @Override
    public void onEnable() {
        Main.instance = this;

        supportQualityArmory = Bukkit.getPluginManager().isPluginEnabled("QualityArmory");

        // register serializable objects
        ConfigurationSerialization.registerClass(Data.class, "Data");
        ConfigurationSerialization.registerClass(LootSet.class, "LootSet");
        ConfigurationSerialization.registerClass(Crate.class, "Crate");

        // api for easy
        LootCratesAPI.registerLoot(LootItemCrate.class, "LootItemCrate");
        LootCratesAPI.registerLoot(LootItem.class, "LootItem");
        if (supportQualityArmory)
            LootCratesAPI.registerLoot(LootItemQA.class, "LootItemQA");

        //org.bukkit.configuration.InvalidConfigurationException

        this.reloadConfig();

        //debug("keyset: " + data.crates.keySet());
        if (data == null) {

        }

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
            debug(e);
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
        // If replacing, then save
        // If back up failed, then save
        if ((replace && backupConfig(replace)) || !configFile.exists()) {
            info("Saving default config");
            this.saveResource("config.yml", true);
        }
    }

    int crashNext = 2;
    @Override
    public void reloadConfig() {
        if (crashNext-- == 0) {
            //crash();
            return;
        }
        // Load file from jar if it doesn't exist
        saveDefaultConfig(false);

        this.config = new YamlConfiguration();

        // Parse
        try {
            config.load(configFile);
            data = (Data) config.get("data");

            if (data == null) {
                throw new NullDataException();
            }
            crashNext = 2;
        } catch (IOException | StackOverflowError e) {
            e.printStackTrace();
            crash();
        } catch (InvalidConfigurationException e) {
            error("Malformed config.yml (saving default config...)");
            e.printStackTrace();

            // then try loading again
            saveDefaultConfig(true);
            this.reloadConfig();
        } catch (NullDataException e) {
            error("Failed to serialize Main.get().data (saving default config...)");

            e.printStackTrace();

            // then try loading again
            saveDefaultConfig(true);
            this.reloadConfig();
        }
    }

    public boolean backupConfig(boolean isFailed) {

        File backupFile = new File(Main.get().getDataFolder(),(isFailed ? "backup/broken_" : "backup/old_") + System.currentTimeMillis() + "_config.yml");

        // create the backup path
        //new File(Main.get().getDataFolder(),"backup/").mkdirs();
        try {
            // try to create the backup
            if (backupFile.getParentFile().mkdirs() && configFile.exists()) {
                info("Backing up config");
                if (backupFile.createNewFile())
                    // copy the old to the new
                    Util.copy(new FileInputStream(configFile), new FileOutputStream(backupFile));
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void saveConfig() {
        // if a backup was successfully made, then save
        if (backupConfig(false)) {
            this.getConfig().set("data", data);

            try {
                this.getConfig().save(configFile);
            } catch (Exception e) {
                error("Failed to save config");
                debug(e);
            }
        }
    }

    @Override
    public FileConfiguration getConfig() {
        if (this.config == null) {
            this.reloadConfig();
        }
        return this.config;
    }

    public void crash() {
        Bukkit.getPluginManager().disablePlugin(this);
    }

    public void info(String s) {
        Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.DARK_GRAY + s);
    }

    public void important(String s) {
        Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.DARK_PURPLE + s);
    }

    public void error(String s) {
        Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.RED + s);
    }

    public void debug(String s) {
        if (data.debug)
            Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.GOLD + s);
    }

    public void debug(Exception e) {
        if (data.debug)
            e.printStackTrace();
    }

}