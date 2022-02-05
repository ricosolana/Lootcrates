package com.crazicrafter1.lootcrates;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.crutils.ReflectionUtil;
import com.crazicrafter1.crutils.Util;
import com.crazicrafter1.lootcrates.cmd.Cmd;
import com.crazicrafter1.lootcrates.crate.ActiveCrate;
import com.crazicrafter1.lootcrates.crate.Crate;
import com.crazicrafter1.lootcrates.crate.LootSet;
import com.crazicrafter1.lootcrates.crate.loot.*;
import com.crazicrafter1.lootcrates.listeners.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
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

    @Override
    public void onEnable() {
        // Check for updates
        GithubUpdater.autoUpdate(this, "PeriodicSeizures", "LootCrates", "LootCrates.jar");

        boolean installedDepends = false;

        // Try to auto install dependencies
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

        // Register serializable objects
        ConfigurationSerialization.registerClass(Data.class, "Data");
        ConfigurationSerialization.registerClass(LootSet.class, "LootSet");
        ConfigurationSerialization.registerClass(Crate.class, "Crate");

        // api for easy
        LootCratesAPI.registerLoot(LootItemCrate.class, new ItemBuilder(Material.CHEST).name("&eAdd crate...").toItem(), "LootItemCrate");
        LootCratesAPI.registerLoot(LootItem.class, new ItemBuilder(Material.GOLD_NUGGET).name("&6Add item...").toItem(), "LootItem");
        LootCratesAPI.registerLoot(LootCommand.class, new ItemBuilder(Material.CHAIN_COMMAND_BLOCK).name("&2Add command...").toItem(), "LootCommand");
        if (supportQualityArmory)
            LootCratesAPI.registerLoot(LootItemQA.class, new ItemBuilder(Material.CROSSBOW).name("&8Add QualityArmory...").toItem(), "LootItemQA");

        // Load Skript classes
        if (supportSkript) {
            addon = Skript.registerAddon(this);
            try {
                addon.loadClasses("com.crazicrafter1.lootcrates", "sk");
            } catch (Exception e) {
                e.printStackTrace();
            }
            LootCratesAPI.registerLoot(LootSkriptEvent.class, new ItemBuilder(Material.MAP).name("&aAdd Skript tag... ").toItem(), "LootSkriptEvent");
        }

        loadExternalLoots();

        reloadConfig();

        /*
         * bStats metrics init
         */
        try {
            Metrics metrics = new Metrics(this, 10395);

            metrics.addCustomChart(new Metrics.SimplePie("updater",
                    () -> "" + data.update));

            metrics.addCustomChart(new Metrics.SimplePie("crates",
                    () -> "" + data.crates.size()));

            metrics.addCustomChart(new Metrics.SimplePie("abstractloots",
                    () -> "" + LootCratesAPI.lootClasses.size()));

            // fun little infinite counter that really has no huge use
            metrics.addCustomChart(new Metrics.SingleLineChart("opened",
                    () -> data.totalOpens));
        } catch (Exception e) {
            error("An error occurred while enabling metrics");
            debug(e);
        }

        /*
         * Command init
         */
        new Cmd(this);

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
     * I recommend using Skript instead of this
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

    @Override
    public void reloadConfig() {
        this.config = new YamlConfiguration();

        int configAttempt = CFG_WAIT;
        while (++configAttempt != CFG_ERR) {
            try {
                switch (configAttempt) {
                    case CFG_CURR:
                        info("Attempt 1: Loading current or default config");

                        saveDefaultConfig(false);
                        config.load(configFile);
                        data = (Data) config.get("data");
                        return;
                    case CFG_DEF:
                        info("Attempt 2: Force loading default config");

                        saveDefaultConfig(true);
                        config.load(configFile);
                        data = (Data) config.get("data");
                        return;
                    case CFG_POP:
                        info("Attempt 3: Populating config with minimal built-ins");

                        data = new Data();
                        data.populate();
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

    @Deprecated
    public void debug(Exception e) {
        if (data.debug)
            e.printStackTrace();
    }

}