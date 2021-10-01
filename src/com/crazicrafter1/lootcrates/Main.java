package com.crazicrafter1.lootcrates;

<<<<<<< HEAD
import java.io.File;
=======
>>>>>>> 2325e3569993e0402afc754541d97cb10307c109
import java.util.*;

import com.crazicrafter1.crutils.*;
import com.crazicrafter1.lootcrates.crate.ActiveCrate;
<<<<<<< HEAD
import com.crazicrafter1.lootcrates.crate.Crate;
import com.crazicrafter1.lootcrates.crate.LootGroup;
import com.crazicrafter1.lootcrates.crate.loot.LootItemCrate;
import com.crazicrafter1.lootcrates.crate.loot.LootItemQA;
import com.crazicrafter1.lootcrates.crate.loot.LootOrdinateItem;
=======
>>>>>>> 2325e3569993e0402afc754541d97cb10307c109
import com.crazicrafter1.lootcrates.tabs.TabCrates;
import com.crazicrafter1.lootcrates.commands.CmdCrates;
import com.crazicrafter1.lootcrates.listeners.*;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
<<<<<<< HEAD
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
=======
>>>>>>> 2325e3569993e0402afc754541d97cb10307c109
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin
{
    /*
     * Runtime modifiable stuff
     */
    public static HashMap<java.util.UUID, ActiveCrate> openCrates = new HashMap<>();
    public static HashSet<UUID> crateFireworks = new HashSet<>();
    public static boolean supportQualityArmory = false;
    public static boolean supportGapi = false;

    VersionChecker updater = new VersionChecker(this, 68424);
    public FileConfiguration config;
    public final String prefix = ChatColor.GRAY + "[" + ChatColor.AQUA + ChatColor.BOLD + "LootCrates" + ChatColor.GRAY + "] ";

    /*
     * Serializable stuff
     */
<<<<<<< HEAD
    public static Data DAT = null;
=======
    public static Data DAT = new Data();
>>>>>>> 2325e3569993e0402afc754541d97cb10307c109

    private static Main main;
    public static Main getInstance() {
        return main;
    }

    @Override
    public void onEnable() {
        Main.main = this;

        Main.main = this;

        /*
         * 1.17 assert
         */
        if(ReflectionUtil.isOldVersion()) {
            Main.getInstance().error(
                    "only MC 1.17+ is supported (Java 16)\n" +
                    "please use LootCrates 3.1.4 and disable auto-update for legacy versions");

            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        supportQualityArmory = Bukkit.getPluginManager().isPluginEnabled("QualityArmory");

        // register serializable objects
        ConfigurationSerialization.registerClass(Data.class);
        ConfigurationSerialization.registerClass(LootGroup.class);
        ConfigurationSerialization.registerClass(LootItemCrate.class);
        ConfigurationSerialization.registerClass(LootOrdinateItem.class);
        ConfigurationSerialization.registerClass(LootItemQA.class);
        ConfigurationSerialization.registerClass(Crate.class);

        if (!new File(getDataFolder(), "config.yml").exists()) {
            saveDefaultConfig();
        }

<<<<<<< HEAD
        reloadConfig();

=======
>>>>>>> 2325e3569993e0402afc754541d97cb10307c109
        if (!DAT.update) {
            try {
                if (updater.hasNewUpdate()) {
                    important("New update : " + updater.getLatestVersion() + ChatColor.DARK_BLUE + " (" + updater.getResourceURL() + ")");

                } else {
                    info("LootCrates is up-to-date!");
                }

            } catch (Exception e) {
                error("An error occurred while checking for updates");
                if (DAT.debug)
                    e.printStackTrace();
            }
        } else
            GithubUpdater.autoUpdate(this, updater, "PeriodicSeizures", "LootCrates", "LootCrates.jar");

        /*
         * bStats metrics init
         *  - key changes that should be recorded:
         *      ghyyuuu854
         */
        try {
            Metrics metrics = new Metrics(this, 10395);
            metrics.addCustomChart(new Metrics.SimplePie("updater", // what to record
                    () -> "" + DAT.update));

            info("Metrics was successfully enabled");
        } catch (Exception e) {
            error("An error occurred while enabling metrics");
            if (DAT.debug)
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
<<<<<<< HEAD
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        this.config = this.getConfig();

        DAT = (Data) config.get("dat");
    }

    @Override
    public void saveConfig() {
        config.set("dat", DAT);
        super.saveConfig();
=======
>>>>>>> 2325e3569993e0402afc754541d97cb10307c109
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
        if (DAT.debug)
            Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.GOLD + s);
    }

}