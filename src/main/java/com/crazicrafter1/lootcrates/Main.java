package com.crazicrafter1.lootcrates;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import com.crazicrafter1.crutils.*;
import com.crazicrafter1.lootcrates.cmd.Cmd;
import com.crazicrafter1.lootcrates.crate.ActiveCrate;
import com.crazicrafter1.lootcrates.crate.Crate;
import com.crazicrafter1.lootcrates.crate.LootSet;
import com.crazicrafter1.lootcrates.crate.loot.*;
import com.crazicrafter1.lootcrates.listeners.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Main extends JavaPlugin
{
    public static final int REV_LATEST = 2;
    private final File configFile = new File(getDataFolder(), "config.yml");
    private final File playerFile = new File(getDataFolder(), "player_stats.yml");
    private final File backupPath = new File(getDataFolder(), "backup");
    private final File revFile = new File(getDataFolder(), "rev.yml");
    private FileConfiguration config = null;

    /*
     * Runtime modifiable stuff
     */
    public HashMap<UUID, ActiveCrate> openCrates = new HashMap<>();
    public HashSet<UUID> crateFireworks = new HashSet<>();
    public boolean supportQualityArmory = false;
    public boolean supportSkript = false;
    public boolean supportMMOItems = false;

    public SkriptAddon addon;

    public Data data;
    private final HashMap<UUID, PlayerStat> playerStats = new HashMap<>();
    public int rev = -1;

    @Nonnull
    public PlayerStat getStat(UUID uuid) {
        // if not present add

        PlayerStat stat = playerStats.get(uuid);
        if (stat == null) {
            stat = new PlayerStat();
            playerStats.put(uuid, stat);
        }

        return stat;
    }

    private static Main instance;
    public static Main get() {
        return instance;
    }

    @Override
    public void onEnable() {
        Plugin GAPI = Bukkit.getPluginManager().getPlugin("Gapi");
        if (GAPI == null || !GAPI.isEnabled()) {
            if (GAPI == null) {
                error("Plugin Gapi is required");
                error("Install it from here " + ChatColor.UNDERLINE + "https://github.com/PeriodicSeizures/Gapi/releases");
            } else
                error("Gapi failed to enable");

            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        info("Join the " + ChatColor.DARK_GRAY + ChatColor.BOLD + "Discord " + ChatColor.RESET + "for help or whatever " + ChatColor.UNDERLINE + "https://discord.gg/2JkFBnyvNQ");

        // Check for updates
        // Look for a file named NO_UPDATE
        getDataFolder().mkdirs();
        File noUpdateFile = new File(getDataFolder(), "NO_UPDATE.txt");
        boolean update = !(noUpdateFile.exists() && noUpdateFile.isFile());
        if (update) try {
                StringBuilder outTag = new StringBuilder();
                if (GitUtils.updatePlugin(this, "PeriodicSeizures", "Lootcrates", "Lootcrates.jar", outTag)) {
                    warn("Updated to " + outTag + "; restart server to use");

                    Bukkit.getPluginManager().disablePlugin(this);
                    return;
                } else {
                    info("Using the latest version");
                }
            } catch (IOException e) {
                warn("Error while updating");
                e.printStackTrace();
            }
        else {
            warn("Updating is disabled (delete " + noUpdateFile.getName() + " to enable)");
            GitUtils.checkForUpdateAsync(this, "PeriodicSeizures", "Lootcrates", (result, tag) -> popup("Update " + tag + " is available"));
        }

        String COLOR = ColorUtil.render(SPLASH);

        if (Version.AT_LEAST_v1_16.a())
            Bukkit.getConsoleSender().sendMessage("\n\n\n\n" + COLOR + "\n\n\n\n");
        else {
            String STRIP = ColorUtil.strip(COLOR);
            int i = STRIP.indexOf("\n") / 3;

            Bukkit.getConsoleSender().sendMessage("\n\n\n\n"
                    + Arrays.stream(STRIP.split("\n")).map(
                            s -> ChatColor.AQUA + s.substring(0, i) + ChatColor.BLUE + s.substring(i, 2 * i) + ChatColor.DARK_BLUE + s.substring(2 * i))
                    .collect(Collectors.joining("\n")) + "\n\n\n\n");
        }

        Main.instance = this;

        supportQualityArmory = Bukkit.getPluginManager().isPluginEnabled("QualityArmory");
        supportSkript = Bukkit.getPluginManager().isPluginEnabled("Skript");
        supportMMOItems = Bukkit.getPluginManager().isPluginEnabled("MMOItems");

        // Register serializable objects
        ConfigurationSerialization.registerClass(Data.class, "Data");
        ConfigurationSerialization.registerClass(LootSet.class, "LootSet");
        ConfigurationSerialization.registerClass(Crate.class, "Crate");

        // api for easy
        LootCratesAPI.registerLoot(LootItemCrate.class);
        LootCratesAPI.registerLoot(LootItem.class);
        LootCratesAPI.registerLoot(LootCommand.class);

        if (supportQualityArmory)
            LootCratesAPI.registerLoot(LootItemQA.class);

        // Load Skript classes
        if (supportSkript) {
            addon = Skript.registerAddon(this);
            try {
                addon.loadClasses("com.crazicrafter1.lootcrates", "sk");
            } catch (Exception e) {
                e.printStackTrace();
            }
            LootCratesAPI.registerLoot(LootSkriptEvent.class);
        }

        if (supportMMOItems)
            LootCratesAPI.registerLoot(LootMMOItem.class);

        // Issues with automatic revision detection:
        // revision file does not exist before rev2
        // revision exists in config in rev2

        // If there is no rev file, this could mean two things:
        // the plugin is a fresh deployment (or config does not exist)
        // the config is rev 1 or older revision
        //

        reloadConfig();

        //if (data == null) {
        //    return;
        //}

        /*
         * bStats metrics init
         */
        MetricWrap.init(this, update);

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
        new ListenerOnPlayerJoinQuit(this);
    }

    // -1 on indefinite revision
    private boolean findRev() {
        if (rev >= 0)
            return true;

        // look for config
        if (!configFile.exists()) {
            rev = REV_LATEST;
            return true;
        }

        if (revFile.exists()) {
            FileConfiguration revConfig = new YamlConfiguration();
            try {
                revConfig.load(revFile);
                rev = revConfig.getInt("rev", REV_LATEST);
            } catch (IOException ignored) {}
            catch (InvalidConfigurationException e) {
                error("Unable to parse " + revFile.getName() + ": " + e.getMessage());
                rev = REV_LATEST;
            }
            return true;
        }

        rev = -1;

        return false;
    }

    private static final String SPLASH =
            " &#adf3fd_&#aaf1fd_      &#a7eefd_&#a4ecfd_&#a1e9fd_&#9ee7fd_&#9be4fd_&#98e2fd_  &#95dffd_&#92ddfd_&#8fdafd_&#8cd8fd_&#89d5fd_&#86d3fd_  &#83d0fc_&#80cefc_&#7dcbfc_&#7ac9fc_&#77c6fc_&#74c4fc_ &#71c1fc_&#6ebffc_&#6bbcfc_&#68bafc_&#65b7fc_&#62b5fc_  &#5fb2fc_&#5cb0fc_&#5aadfc_&#57abfc_&#54a8fc_&#51a6fc_  &#4ea3fc_&#4ba1fc_&#489efc_&#459cfc_&#4299fc_&#3f97fc_  &#3c94fc_&#3992fc_&#368ffc_&#338dfc_&#308afb_&#2d88fb_ &#2a85fb_&#2783fb_&#2480fb_&#217efb_&#1e7bfb_&#1b79fb_  &#1876fb_&#1574fb_&#1271fb_&#0f6ffb_&#0c6cfb_&#096afb_\n" +
                    "&#adf3fd/&#aaf0fd\\ &#a6eefd\\    &#a3ebfd/&#a0e8fd\\  &#9de5fd_&#99e3fd_ &#96e0fd\\&#93ddfd/&#8fdafd\\  &#8cd8fd_&#89d5fd_ &#86d2fd\\&#82cffc/&#7fcdfc\\&#7ccafc_&#79c7fc_  &#75c4fc_&#72c2fc/&#6fbffc\\  &#6bbcfc_&#68b9fc_&#65b7fc_&#62b4fc\\&#5eb1fc/&#5baffc\\  &#58acfc=&#54a9fc= &#51a6fc\\&#4ea4fc/&#4ba1fc\\  &#479efc_&#449bfc_ &#4199fc\\&#3d96fc/&#3a93fc\\&#3790fc_&#348efc_  &#308bfb_&#2d88fb/&#2a85fb\\  &#2783fb_&#2380fb_&#207dfb_&#1d7afb\\&#1978fb/&#1675fb\\  &#1372fb_&#106ffb_&#0c6dfb_&#096afb\\   \n" +
                    "&#adf3fd\\ &#aaf0fd\\ &#a7eefd\\&#a4ebfd_&#a1e9fd_&#9ee6fd_&#9ae3fd\\ &#97e1fd\\ &#94defd\\&#91dcfd/&#8ed9fd\\ &#8bd7fd\\ &#88d4fd\\ &#85d1fd\\&#82cffc/&#7fccfc\\ &#7bcafc\\&#78c7fc/&#75c4fc_&#72c2fc/&#6fbffc\\ &#6cbdfc\\&#69bafc\\ &#66b8fc\\ &#63b5fc\\&#60b2fc_&#5db0fc_&#59adfc_&#56abfc\\ &#53a8fc\\  &#50a5fc_&#4da3fc_&#4aa0fc<&#479efc\\ &#449bfc\\  &#4199fc_&#3e96fc_ &#3b93fc\\&#3791fc/&#348efc_&#318cfb/&#2e89fb\\ &#2b86fb\\&#2884fb\\ &#2581fb\\  &#227ffb_&#1f7cfb_&#1c7afb\\&#1877fb\\ &#1574fb\\&#1272fb_&#0f6ffb_&#0c6dfb_  &#096afb\\  \n" +
                    " &#adf3fd\\ &#aaf1fd\\&#a8effd_&#a5edfd_&#a3ebfd_&#a0e8fd_&#9ee6fd_&#9be4fd\\ &#99e2fd\\&#96e0fd_&#94defd_&#91dcfd_&#8fdafd_&#8cd8fd_&#8ad5fd\\ &#87d3fd\\&#85d1fd_&#82cffc_&#80cdfc_&#7dcbfc_&#7bc9fc_&#78c7fc\\ &#75c5fc\\ &#73c3fc\\&#70c0fc_&#6ebefc\\&#6bbcfc\\ &#69bafc\\&#66b8fc_&#64b6fc_&#61b4fc_&#5fb2fc_&#5cb0fc_&#5aadfc\\ &#57abfc\\&#55a9fc_&#52a7fc\\ &#50a5fc\\&#4da3fc_&#4ba1fc\\ &#489ffc\\&#469dfc_&#439afc\\ &#4198fc\\&#3e96fc_&#3b94fc\\ &#3992fc\\ &#3690fc\\&#348efc_&#318cfb\\&#2f8afb\\ &#2c88fb\\&#2a85fb_&#2783fb_&#2581fb_&#227ffb_&#207dfb_&#1d7bfb\\&#1b79fb/&#1877fb\\&#1675fb_&#1372fb_&#1170fb_&#0e6efb_&#0c6cfb_&#096afb\\ \n" +
                    "  &#adf3fd\\&#abf1fd/&#a8effd_&#a6edfd_&#a4ebfd_&#a1e9fd_&#9fe7fd_&#9de5fd/&#9ae3fd\\&#98e1fd/&#96dffd_&#93ddfd_&#91dcfd_&#8fdafd_&#8cd8fd_&#8ad6fd/&#88d4fd\\&#85d2fd/&#83d0fc_&#80cefc_&#7eccfc_&#7ccafc_&#79c8fc_&#77c6fc/  &#75c4fc\\&#72c2fc/&#70c0fc_&#6ebefc/ &#6bbcfc\\&#69bafc/&#67b8fc_&#64b6fc_&#62b4fc_&#60b2fc_&#5db0fc_&#5baffc/&#59adfc\\&#56abfc/&#54a9fc_&#52a7fc/ &#4fa5fc/&#4da3fc_&#4ba1fc/&#489ffc\\&#469dfc/&#449bfc_&#4199fc/&#3f97fc\\&#3d95fc/&#3a93fc_&#3891fc/  &#368ffc\\&#338dfc/&#318bfb_&#2e89fb/ &#2c87fb\\&#2a85fb/&#2783fb_&#2581fb_&#2380fb_&#207efb_&#1e7cfb_&#1c7afb/&#1978fb\\&#1776fb/&#1574fb_&#1272fb_&#1070fb_&#0e6efb_&#0b6cfb_&#096afb/ ";

    @Override
    public void onDisable() {
        if (instance == null)
            return;

        this.saveConfig();
    }

    @Override
    @Deprecated
    public void saveDefaultConfig() {
        throw new RuntimeException("Do not call this method");
    }

    public void saveDefaultConfig(boolean replace) {
        saveDefaultConfig(replace, true);
    }

    public void saveDefaultConfig(boolean replace, boolean verbose) {
        // If replacing, then save
        // If back up failed, then save
        if ((replace && backupConfig(replace)) || !configFile.exists()) {
            if (verbose) info("Saving default config");
            this.saveResource(configFile.getName(), true);
        }
    }

    @Override
    public void reloadConfig() {
        this.config = new YamlConfiguration();

        if (!findRev()) {
            // then we have a problem
            // must ask for revision
            error("Unable to detect revision");
            return;
        }

        loadPlayerStats();

        try {
            info("Attempt 1: Loading config");
            saveDefaultConfig(false, false);
            config.load(configFile);
            data = (Data) config.get("data");
        } catch (Exception e) {
            //error(e.getMessage());
            try {
                popup("Attempt 2: Loading default config");

                saveDefaultConfig(true, false);
                config.load(configFile);
                data = (Data) config.get("data");
            } catch (Exception e1) {
                //error(e1.getMessage());
                try {
                    warn("Attempt 3: Populating config with defaults");

                    data = new Data();
                } catch (Exception e2) {
                    // Very severe, should theoretically never reach this point
                    e2.printStackTrace();
                }
            }
        }

        if (data != null) {
            info("Successfully loaded config");
            return;
        }

        severe("All fallback attempts failed");
        severe("Please report this at https://discord.gg/2JkFBnyvNQ");
        Bukkit.getPluginManager().disablePlugin(this);
    }

    public boolean backupConfig(boolean isBroken) {
        File backupFile = new File(backupPath, System.currentTimeMillis() + "_" + (isBroken ? "broken" : "old") + "_config.zip");

        info("Making a backup of the config");

        return Util.backupZip(configFile, backupFile);
    }

    @Override
    public void saveConfig() {
        // if a backup was successfully made, then save

        if (rev == -1)
            return;

        // save
        FileConfiguration revConfig = new YamlConfiguration();
        revConfig.set("rev", REV_LATEST);
        try {
            revConfig.save(revFile);
        } catch (IOException e) {
            error("Unable to save " + revFile.getName() + ": " + e.getMessage());
        }

        savePlayerStats();

        if (backupConfig(false)) {
            info("Saving config...");
            config.set("data", data);

            try {
                config.save(configFile);
            } catch (Exception e) {
                error("Failed to save config");
                e.printStackTrace();
            }
        } else error("Config was not backed up");

        deleteOldBackups();
    }

    private static final Pattern BACKUP_PATTERN = Pattern.compile("([0-9])+_\\S+_config.zip");
    private void deleteOldBackups() {
        int deletedCount = 0;
        try {
            backupPath.mkdirs();

            //noinspection ConstantConditions
            for (File file : backupPath.listFiles()) {
                String name = file.getName();
                Matcher matcher = BACKUP_PATTERN.matcher(name);
                if (matcher.matches()) {
                    long create = Long.parseLong(name.substring(0, name.indexOf("_")));
                    if (create < System.currentTimeMillis() - (data.cleanAfterDays * 24 * 60 * 60 * 1000)) {
                        // delete it
                        file.delete();
                        deletedCount++;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (deletedCount > 0)
            info("Purged " + deletedCount + " old configurations");
        else info("No old configurations to purge");
    }

    private void savePlayerStats() {
        try {
            YamlConfiguration playerConfig = new YamlConfiguration();

            for (Map.Entry<UUID, PlayerStat> entry : playerStats.entrySet()) {
                String uuid = entry.getKey().toString();
                playerConfig.set(uuid + ".editorMessaged",
                        entry.getValue().editorMessaged);
                for (Map.Entry<String, Integer> entry1 : entry.getValue().openedCrates.entrySet()) {
                    playerConfig.set(uuid + ".crates." + entry1.getKey(),
                            entry1.getValue());
                }
            }

            playerConfig.save(playerFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadPlayerStats() {
        try {
            if (!playerFile.exists())
                return;

            YamlConfiguration playerConfig = new YamlConfiguration();
            playerConfig.load(playerFile);

            for (String uuid : playerConfig.getKeys(false)) {
                PlayerStat stat = new PlayerStat();
                playerStats.put(UUID.fromString(uuid), stat);
                stat.editorMessaged = playerConfig.getBoolean(uuid + ".editorMessaged");
                ConfigurationSection section = playerConfig.getConfigurationSection(uuid + ".crates");
                if (section != null)
                    for (String id : section.getKeys(false)) {
                        stat.openedCrates.put(id, playerConfig.getInt(uuid + ".crates." + id));
                    }
            }
        } catch (Exception e) {e.printStackTrace();}
    }

    @Override
    public FileConfiguration getConfig() {
        if (this.config == null) {
            this.reloadConfig();
        }
        return this.config;
    }

    public void popup(String s) {
        popup(Bukkit.getConsoleSender(), s);
    }

    public void info(String s) {
        info(Bukkit.getConsoleSender(), s);
    }

    public void warn(String s) {
        warn(Bukkit.getConsoleSender(), s);
    }

    public void error(String s) {
        error(Bukkit.getConsoleSender(), s);
    }

    public void severe(String s) {
        severe(Bukkit.getConsoleSender(), s);
    }

    public boolean popup(CommandSender sender, String s) {
        sender.sendMessage(
                (sender instanceof ConsoleCommandSender ? ChatColor.WHITE + "[" + ChatColor.GREEN + "LC" + ChatColor.WHITE + "]" : "" + ChatColor.DARK_GRAY + ChatColor.BOLD + "\u24D8") + " "
                        + ChatColor.RESET + ChatColor.AQUA + s);
        return true;
    }

    public boolean info(CommandSender sender, String s) {
        sender.sendMessage(
                (sender instanceof ConsoleCommandSender ? ChatColor.WHITE + "[" + ChatColor.BLUE + "LC" + ChatColor.WHITE + "]" : "" + ChatColor.DARK_GRAY + ChatColor.BOLD + "\u24D8") + " "
                        + ChatColor.RESET + s);
        return true;
    }

    public boolean warn(CommandSender sender, String s) {
        sender.sendMessage(
                (sender instanceof ConsoleCommandSender ? ChatColor.WHITE + "[" + ChatColor.YELLOW + "LC" + ChatColor.WHITE + "]" : "" + ChatColor.GOLD + ChatColor.BOLD + "\u26A1") + " "
                + ChatColor.RESET + ChatColor.YELLOW + s);
        return true;
    }

    public boolean error(CommandSender sender, String s) {
        sender.sendMessage(
                (sender instanceof ConsoleCommandSender ? ChatColor.WHITE + "[" + ChatColor.RED + "LC" + ChatColor.WHITE + "]" : "" + ChatColor.DARK_RED + ChatColor.BOLD + "\u26A0") + " "
                + ChatColor.RESET + ChatColor.RED + s);
        return true;
    }

    public boolean severe(CommandSender sender, String s) {
        sender.sendMessage(
                (sender instanceof ConsoleCommandSender ? ChatColor.WHITE + "[" + ChatColor.DARK_RED + "LC" + ChatColor.WHITE + "]" : "" + ChatColor.DARK_RED + ChatColor.BOLD + "\u26A0") + " "
                        + ChatColor.RESET + ChatColor.DARK_RED + s);
        return true;
    }
}