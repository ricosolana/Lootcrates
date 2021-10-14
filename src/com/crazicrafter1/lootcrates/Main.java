package com.crazicrafter1.lootcrates;

import com.crazicrafter1.crutils.Metrics;
import com.crazicrafter1.crutils.Util;
import com.crazicrafter1.lootcrates.commands.CmdCrates;
import com.crazicrafter1.lootcrates.crate.ActiveCrate;
import com.crazicrafter1.lootcrates.crate.Crate;
import com.crazicrafter1.lootcrates.crate.LootSet;
import com.crazicrafter1.lootcrates.crate.loot.ILoot;
import com.crazicrafter1.lootcrates.crate.loot.LootItem;
import com.crazicrafter1.lootcrates.crate.loot.LootItemCrate;
import com.crazicrafter1.lootcrates.crate.loot.LootItemQA;
import com.crazicrafter1.lootcrates.listeners.*;
import com.crazicrafter1.lootcrates.tabs.TabCrates;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
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

    private final static int CFG_WAIT = -1;
    private final static int CFG_CURR = 0;
    private final static int CFG_DEF = 1;
    private final static int CFG_POP = 2;
    private final static int CFG_ERR = 3;
    private int configAttempt;

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

        loadExternalLoots();

        reloadConfig();

        //new Updater(this, "PeriodicSeizures", "LootCrates", data.update);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (data.update)
                    if (GithubUpdater.autoUpdate(instance, "PeriodicSeizures", "LootCrates", "LootCrates.jar"))
                        cancel();
            }
        }.runTaskTimer(this, 1, 20 * 60 * 60 * 24);

        /*
         * bStats metrics init
         */
        try {
            Metrics metrics = new Metrics(this, 10395);

            metrics.addCustomChart(new Metrics.SimplePie("updater", // what to record
                    () -> "" + data.update));

            metrics.addCustomChart(new Metrics.SimplePie("crates", // what to record
                    () -> "" + data.crates.size()));

            metrics.addCustomChart(new Metrics.SimplePie("abstractloots",
                    () -> "" + LootCratesAPI.lootClasses.size()));

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

    private void loadExternalLoots() {
        try {
            File file = new File(getDataFolder(), "loots.csv");

            if (file.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(file));

                String line;
                while ((line = reader.readLine()) != null && !line.isEmpty()) {
                    String[] split = line.split(",");

                    Class<? extends ILoot> clazz = (Class<? extends ILoot>) Class.forName(split[0]);
                    String alias = split[1];

                    LootCratesAPI.registerLoot(clazz, alias);
                    info("Loaded external loot: " + clazz.getName() + " as " + alias);
                }
                reader.close();
            }
        } catch (Exception e) {e.printStackTrace();}
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

    //int crashNext = 2;

    @Override
    public void reloadConfig() {
        this.config = new YamlConfiguration();

        configAttempt = CFG_WAIT;
        while (++configAttempt != CFG_ERR) {
            switch (configAttempt) {
                case CFG_CURR:
                    info("Attempt 1: Loading current or default config");
                    try {
                        saveDefaultConfig(false);
                        config.load(configFile);
                        data = (Data) config.get("data");
                        configAttempt = CFG_WAIT;
                        return;
                    } catch (Exception e) {
                        error(e.getMessage());
                    }
                    break;
                case CFG_DEF:
                    info("Attempt 2: Force loading default config");
                    try {
                        saveDefaultConfig(true);
                        config.load(configFile);
                        data = (Data) config.get("data");
                        configAttempt = CFG_WAIT;
                        return;
                    } catch (Exception e) {
                        error(e.getMessage());
                    }
                    break;
                case CFG_POP:
                    info("Attempt 3: Populating config with minimal built-ins");

                    try {
                        data = new Data();
                        data.populate();
                        configAttempt = CFG_WAIT;
                        return;
                    } catch (Exception e) {
                        error(e.getMessage());
                        break;
                    }
            }
        }

        error("Failed all contingency attempts, plugin will now disable...");
        Bukkit.getPluginManager().disablePlugin(this);
    }

    public boolean backupConfig(boolean isBroken) {
        File backupFile = new File(Main.get().getDataFolder(),(isBroken ? "backup/broken_" : "backup/old_") + System.currentTimeMillis() + "_config.yml");

        try {
            // Create path
            backupFile.getParentFile().mkdirs();

            if (configFile.exists()) {
                info("Backing up config");

                // Create backup
                backupFile.createNewFile();

                // Copy files
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
            info("Saving config...");
            //this.config = new YamlConfiguration();
            config.set("data", data);

            try {
                config.save(configFile);
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
            //this.config = new YamlConfiguration();
        }
        return this.config;
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