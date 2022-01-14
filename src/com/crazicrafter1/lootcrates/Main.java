package com.crazicrafter1.lootcrates;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import com.crazicrafter1.crutils.ReflectionUtil;
import com.crazicrafter1.crutils.Util;
import com.crazicrafter1.lootcrates.commands.Cmd;
import com.crazicrafter1.lootcrates.crate.ActiveCrate;
import com.crazicrafter1.lootcrates.crate.Crate;
import com.crazicrafter1.lootcrates.crate.LootSet;
import com.crazicrafter1.lootcrates.crate.loot.*;
import com.crazicrafter1.lootcrates.listeners.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class Main extends JavaPlugin
{
    public String prefix() {
        return prefix;
    }

    // Default prefix as a fallback if no CRUtils found during startup
    // and for non 1.16+ versions
    private String prefix = ChatColor.translateAlternateColorCodes('&', "&f[&b&lLootCrates&r&f] ");

    /*
     * Runtime modifiable stuff
     */
    public HashMap<UUID, ActiveCrate> openCrates = new HashMap<>();
    public HashSet<UUID> crateFireworks = new HashSet<>();
    public boolean supportQualityArmory = false;
    public boolean supportSkript = false;

    public SkriptAddon addon;

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

        GithubUpdater.autoUpdate(this, "PeriodicSeizures", "LootCrates", "LootCrates.jar");

        boolean installedDepends = false;

        if (Bukkit.getPluginManager().getPlugin("CRUtils") == null) {
            GithubInstaller.installDepend(this,
                    "PeriodicSeizures",
                    "CRUtils",
                    "CRUtils.jar",
                    "CRUtils");
            installedDepends = true;
        }

        if (Bukkit.getPluginManager().getPlugin("Gapi") == null) {
            GithubInstaller.installDepend(this,
                    "PeriodicSeizures",
                    "Gapi",
                    "Gapi.jar",
                    "Gapi");
            installedDepends = true;
        }

        if (installedDepends) {
            error(ChatColor.RED + "Please restart server to use plugin");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Cool looking 1.16+ hex colors if possible
        prefix = ReflectionUtil.isAtLeastVersion("1_16") ?
                Util.format("&f[" + "&#fba600L&#fb9400o&#fb8100o&#fc6f00t&#fc5c00c&#fc4a00r&#fc3700a&#fd2500t&#fd1200e&#fd0000s" + "&f] ") :
                prefix;

        Main.instance = this;

        supportQualityArmory = Bukkit.getPluginManager().isPluginEnabled("QualityArmory");
        supportSkript = Bukkit.getPluginManager().isPluginEnabled("Skript");

        // register serializable objects
        ConfigurationSerialization.registerClass(Data.class, "Data");
        ConfigurationSerialization.registerClass(LootSet.class, "LootSet");
        ConfigurationSerialization.registerClass(Crate.class, "Crate");

        // api for easy
        LootCratesAPI.registerLoot(LootItemCrate.class, "LootItemCrate");
        LootCratesAPI.registerLoot(LootItem.class, "LootItem");
        if (supportQualityArmory)
            LootCratesAPI.registerLoot(LootItemQA.class, "LootItemQA");

        if (supportSkript) {
            addon = Skript.registerAddon(this);
            try {
                addon.loadClasses("com.crazicrafter1.lootcrates", "sk");
            } catch (Exception e) {
                e.printStackTrace();
            }
            LootCratesAPI.registerLoot(LootSkriptEvent.class, "LootSkriptEvent");
        }

        loadExternalLoots();

        reloadConfig();

        //new Updater(this, "PeriodicSeizures", "LootCrates", data.update);

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
        //new CmdCrates();
        new Cmd(this);
        //new TabCrates();

        /*
         * Listener init
         */
        new ListenerOnEntityDamageByEntity(this);
        new ListenerOnInventoryClick(this);
        new ListenerOnInventoryClose(this);
        new ListenerOnInventoryDrag(this);
        new ListenerOnPlayerInteract(this);
        new ListenerOnPlayerInteract(this);
        new ListenerOnPlayerQuit(this);
    }

    /**
     * Use Skript now,
     * making a plugin for 1 simple event is kinda extensive...
     */
    @Deprecated
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
        if (instance != null)
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
            try {
                switch (configAttempt) {
                    case CFG_CURR:
                        info("Attempt 1: Loading current or default config");

                        saveDefaultConfig(false);
                        config.load(configFile);
                        data = (Data) config.get("data");
                        configAttempt = CFG_WAIT;
                        return;
                    case CFG_DEF:
                        info("Attempt 2: Force loading default config");

                        saveDefaultConfig(true);
                        config.load(configFile);
                        data = (Data) config.get("data");
                        configAttempt = CFG_WAIT;
                        return;
                    case CFG_POP:
                        info("Attempt 3: Populating config with minimal built-ins");

                        data = new Data();
                        data.populate();
                        configAttempt = CFG_WAIT;
                        return;
                }
            } catch (Exception e) {
                error(e.getMessage());
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