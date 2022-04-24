package com.crazicrafter1.lootcrates;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import com.crazicrafter1.crutils.*;
import com.crazicrafter1.lootcrates.cmd.Cmd;
import com.crazicrafter1.lootcrates.cmd.CmdTestParser;
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
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main extends JavaPlugin
{
    private static final String SPLASH =
            " __         ______     ______     ______   ______     ______     ______     ______   ______     ______    \n" +
            "/\\ \\       /\\  __ \\   /\\  __ \\   /\\__  _\\ /\\  ___\\   /\\  == \\   /\\  __ \\   /\\__  _\\ /\\  ___\\   /\\  ___\\   \n" +
            "\\ \\ \\____  \\ \\ \\/\\ \\  \\ \\ \\/\\ \\  \\/_/\\ \\/ \\ \\ \\____  \\ \\  __<   \\ \\  __ \\  \\/_/\\ \\/ \\ \\  __\\   \\ \\___  \\  \n" +
            " \\ \\_____\\  \\ \\_____\\  \\ \\_____\\    \\ \\_\\  \\ \\_____\\  \\ \\_\\ \\_\\  \\ \\_\\ \\_\\    \\ \\_\\  \\ \\_____\\  \\/\\_____\\ \n" +
            "  \\/_____/   \\/_____/   \\/_____/     \\/_/   \\/_____/   \\/_/ /_/   \\/_/\\/_/     \\/_/   \\/_____/   \\/_____/";

    public static final int REV_LATEST = 3;
    private final File rewardsConfigFile = new File(getDataFolder(), "rewards.yml");
    private final File playerStatsFile = new File(getDataFolder(), "player_stats.yml");
    private final File backupPath = new File(getDataFolder(), "backup");
    private final File revFile = new File(getDataFolder(), "rev.yml");
    private final File configFile = new File(getDataFolder(), "config.yml");

    private FileConfiguration config = null;
    private FileConfiguration rewardsConfig = null;

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
    public String language;
    public boolean update;
    public long cleanAfterDays;
    private final HashMap<UUID, PlayerStat> playerStats = new HashMap<>();
    public int rev = -1;

    // -1 on indefinite revision
    private int findRev() {
        if (getDataFolder().listFiles().length == 0)
            return REV_LATEST;

        if (revFile.exists()) {
            FileConfiguration revConfig = new YamlConfiguration();
            try {
                revConfig.load(revFile);
                return revConfig.getInt("rev", REV_LATEST);
            } catch (Exception ignored) {
            }
        }

        error("Unable to determine plugin save revision");
        return -1;
    }

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

        info("Join the " + ChatColor.DARK_GRAY + ChatColor.BOLD + "Discord " + ChatColor.RESET + "for help and more " + ChatColor.UNDERLINE + "https://discord.gg/2JkFBnyvNQ" + ChatColor.RESET);

        //noinspection ResultOfMethodCallIgnored
        getDataFolder().mkdirs();

        this.rev = findRev(); //TODO remove in ~rev4

        reloadConfig(null);

        boolean check = rev == -1 || !update;

        if (rev != -1) {
            if (update) try {
                StringBuilder outTag = new StringBuilder();
                if (GitUtils.updatePlugin(this, "PeriodicSeizures", "Lootcrates", "Lootcrates.jar", outTag)) {
                    warn("Updated to " + outTag);
                    warn("Restarting is recommended to avoid issues");
                } else {
                    info("Using the latest version");
                }
            } catch (IOException e) {
                warn("Error while updating");
                e.printStackTrace();
            }
        }

        if (check)
            GitUtils.checkForUpdateAsync(this, "PeriodicSeizures", "Lootcrates",
                    (result, tag) -> {
                        if (result) popup("Update " + tag + " is available");
                        else info("Using latest version");
                    });

        if (Version.AT_LEAST_v1_16.a()) {
            long c = System.currentTimeMillis();
            final double mul = 1.d / (1000.d * 60.d);
            double h3 = ((double) c) * mul;

            float fh3 = (float)(h3 - Math.floor(h3));

            Color color1 = Color.getHSBColor(fh3, .85f, .75f);
            Color color2 = Color.getHSBColor(fh3 + .3f, .85f, .75f);

            // convert to hex
            String hex1 = ColorUtil.toHex(color1);
            String hex2 = ColorUtil.toHex(color2);

            String[] split = SPLASH.split("\n");
            for (int i = 0; i < split.length; i++) {
                split[i] = ColorUtil.renderAll(String.format("<#%s>%s</#%s>", hex1, split[i], hex2));
            }

            String res = String.join("\n", split);

            Bukkit.getConsoleSender().sendMessage("\n\n\n\n" + res + "\n\n\n\n");
        }

        Main.instance = this;

        supportQualityArmory = Bukkit.getPluginManager().isPluginEnabled("QualityArmory");
        supportSkript = Bukkit.getPluginManager().isPluginEnabled("Skript");
        supportMMOItems = Bukkit.getPluginManager().isPluginEnabled("MMOItems");

        // Register serializable
        ConfigurationSerialization.registerClass(Data.class, "Data"); // TODO try to stray away from this serialize method somehow
        ConfigurationSerialization.registerClass(LootSet.class, "LootSet");
        ConfigurationSerialization.registerClass(Crate.class, "Crate");

        // Register loot classes
        LootCratesAPI.registerLoot(LootCommand.class);
        LootCratesAPI.registerLoot(LootItem.class);
        LootCratesAPI.registerLoot(LootItemCrate.class);
        LootCratesAPI.registerLoot(LootNBTItem.class);
        if (supportQualityArmory) LootCratesAPI.registerLoot(LootItemQA.class);
        if (supportSkript) {
            addon = Skript.registerAddon(this);
            try {
                addon.loadClasses(getClass().getPackage().getName(), "sk");
            } catch (Exception e) {
                e.printStackTrace();
            }
            LootCratesAPI.registerLoot(LootSkriptEvent.class);
        }
        if (supportMMOItems) LootCratesAPI.registerLoot(LootMMOItem.class);

        reloadOtherConfigs(null);

        MetricWrap.init(this);

        new Cmd(this);
        new CmdTestParser(this);

        new ListenerOnEntityDamageByEntity(this);
        new ListenerOnInventoryClick(this);
        new ListenerOnInventoryClose(this);
        new ListenerOnInventoryDrag(this);
        new ListenerOnPlayerInteract(this);
        new ListenerOnPlayerInteract(this);
        new ListenerOnPlayerJoinQuit(this);
    }

    @Override
    public void onDisable() {
        if (instance == null)
            return;

        this.saveConfig(null);
        this.saveOtherConfigs(null);
    }



    @Override
    @Deprecated
    public void saveDefaultConfig() {
        throw new RuntimeException("Do not call this method");
    }

    @Override
    @Deprecated
    public void reloadConfig() {
        throw new RuntimeException("Do not call this method");
    }

    @Override
    @Deprecated
    public void saveConfig() {
        throw new RuntimeException("Do not call this method");
    }



    public void saveDefaultFile(CommandSender sender, File file, boolean replace) {
        if (replace || !file.exists()) {
            info(sender, "Saving default " + file.getName());
            this.saveResource(file.getName(), true);
        }
    }

    public void saveDefaultConfig(CommandSender sender, boolean replace) {
        saveDefaultFile(sender, configFile, replace);
    }

    public boolean backupRewards(CommandSender sender, boolean isBroken) {
        File backupFile = new File(backupPath, System.currentTimeMillis() + "_" + (isBroken ? "broken" : "old") + "_rewards.zip");

        info(sender, "Making a backup of rewards.yml");

        return Util.zip(rewardsConfigFile, backupFile);
    }



    public void reloadConfig(CommandSender sender) {
        if (rev == -1)
            return;

        try {
            if (rev <= 2) {
                // load several
                final File langFile = new File(getDataFolder(), "lang.yml");

                YamlConfiguration langConfig = YamlConfiguration.loadConfiguration(langFile);

                File noUpdateFile = new File(getDataFolder(), "NO_UPDATE.txt");

                this.language = langConfig.getString("language", "en");
                this.update = !(noUpdateFile.exists() && noUpdateFile.isFile());

                langFile.delete();
                noUpdateFile.delete();
            } else if (rev == 3) {
                config = YamlConfiguration.loadConfiguration(configFile);

                this.language = config.getString("language", "en");
                this.update = config.getBoolean("update", false);
                this.cleanAfterDays = config.getInt("clean-after-days", 7);
            }

            //if (rev == 4) {
            //    this.rev = config.getInt("rev");
            //    this.language = config.getString("language");
            //    this.update = config.getBoolean("update");
            //    this.disableOnUpdate = config.getBoolean("disable-on-update");
            //}
        } catch (Exception e) {
            error(sender, "Failed to load: " + e.getMessage());
        }
    }

    public void reloadOtherConfigs(CommandSender sender) {
        if (rev == -1)
            return;

        loadPlayerStats(sender);

        if (rev <= 2) {
            final File rewardsConfigFile_REV2 = new File(getDataFolder(), "config.yml");

            // swap config.yml contents with rewards config in memory
            rewardsConfig = YamlConfiguration.loadConfiguration(rewardsConfigFile_REV2);
        } else {
            saveDefaultFile(sender, rewardsConfigFile, false);

            rewardsConfig = YamlConfiguration.loadConfiguration(rewardsConfigFile);
        }

        try {
            info(sender, "Attempt 1: Loading rewards.yml");
            data = Objects.requireNonNull((Data) rewardsConfig.get("data"));
        } catch (Exception e) {
            //error(sender, e.getMessage());
            e.printStackTrace();
            try {
                popup(sender, "Attempt 2: Loading default rewards.yml");

                saveDefaultFile(sender, rewardsConfigFile, true);
                rewardsConfig.load(rewardsConfigFile);
                data = Objects.requireNonNull((Data) rewardsConfig.get("data"));
            } catch (Exception e1) {
                //error(sender, e1.getMessage());
                e.printStackTrace();
                try {
                    warn(sender, "Attempt 3: Populating with default rewards");

                    data = new Data();
                } catch (Exception e2) {
                    // Very severe, should theoretically never reach this point
                    e2.printStackTrace();
                }
            }
        }

        if (data != null) {
            info(sender, "Successfully loaded rewards.yml");
            return;
        }

        severe(sender, "All fallback attempts failed");
        severe(sender, "Please report this at https://discord.gg/2JkFBnyvNQ");
        Bukkit.getPluginManager().disablePlugin(this);
    }

    public void saveConfig(CommandSender sender) {
        // if a backup was successfully made, then save

        if (rev == -1)
            return;

        // save
        FileConfiguration revConfig = new YamlConfiguration();
        revConfig.set("rev", REV_LATEST);
        try {
            revConfig.save(revFile);
        } catch (IOException e) {
            error(sender, "Unable to save " + revFile.getName() + ": " + e.getMessage());
        }

        try {
            config = new YamlConfiguration();
            config.set("language", language);
            config.set("update", update);
            config.set("clean-after-days", cleanAfterDays);
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveOtherConfigs(CommandSender sender) {
        // if a backup was successfully made, then save

        if (rev == -1)
            return;

        savePlayerStats(sender);

        if (backupRewards(sender, false)) {
            info(sender, "Saving config...");
            rewardsConfig.set("data", data);

            try {
                rewardsConfig.save(rewardsConfigFile);
            } catch (Exception e) {
                error(sender, "Failed to save config");
                e.printStackTrace();
            }
        } else error(sender, "Config was not backed up");

        deleteOldBackups(sender);
    }

    private static final Pattern BACKUP_PATTERN = Pattern.compile("([0-9])+_\\S+_rewards.zip");
    private void deleteOldBackups(CommandSender sender) {
        try {
            int deletedCount = 0;
            backupPath.mkdirs();

            //noinspection ConstantConditions
            for (File file : backupPath.listFiles()) {
                String name = file.getName();
                Matcher matcher = BACKUP_PATTERN.matcher(name);
                if (matcher.matches()) {
                    long create = Long.parseLong(name.substring(0, name.indexOf("_")));
                    if (create < System.currentTimeMillis() - (cleanAfterDays * 24 * 60 * 60 * 1000)) {
                        // delete it
                        file.delete();
                        deletedCount++;
                    }
                }
            }

            if (deletedCount > 0)
                info(sender, "Deleted " + deletedCount + " old configurations");
            else info(sender, "No configurations were deleted");
        } catch (Exception e) {
            error(sender, "Error deleting old backups: " + e.getMessage());
        }
    }

    private void savePlayerStats(CommandSender sender) {
        try {
            YamlConfiguration playerConfig = new YamlConfiguration();

            for (Map.Entry<UUID, PlayerStat> entry : playerStats.entrySet()) {
                String uuid = entry.getKey().toString();
                for (Map.Entry<String, Integer> entry1 : entry.getValue().openedCrates.entrySet()) {
                    playerConfig.set(uuid + ".crates." + entry1.getKey(),
                            entry1.getValue());
                }
            }

            playerConfig.save(playerStatsFile);
        } catch (Exception e) {
            error(sender, "Error saving player stats: " + e.getMessage());
        }
    }

    private void loadPlayerStats(CommandSender sender) {
        try {
            if (!playerStatsFile.exists())
                return;

            YamlConfiguration playerConfig = new YamlConfiguration();
            playerConfig.load(playerStatsFile);

            for (String uuid : playerConfig.getKeys(false)) {
                PlayerStat stat = new PlayerStat();
                playerStats.put(UUID.fromString(uuid), stat);
                ConfigurationSection section = playerConfig.getConfigurationSection(uuid + ".crates");
                if (section != null)
                    for (String id : section.getKeys(false)) {
                        stat.openedCrates.put(id, playerConfig.getInt(uuid + ".crates." + id));
                    }
            }
        } catch (Exception e) {
            error(sender, "Error loading player stats: " + e.getMessage());
        }
    }



    @NotNull
    @Override
    public FileConfiguration getConfig() {
        if (this.config == null) {
            this.reloadConfig(null);
        }
        return this.config;
    }



    public void popup(String s) {
        popup(null, s);
    }

    public void info(String s) {
        info(null, s);
    }

    public void warn(String s) {
        warn(null, s);
    }

    public void error(String s) {
        error(null, s);
    }

    public void severe(String s) {
        severe(null, s);
    }

    public boolean popup(@Nullable CommandSender sender, String s) {
        //sender = Util.def(sender, Bukkit.getConsoleSender());
        if (sender == null)
            sender = Bukkit.getConsoleSender();
        sender.sendMessage(
                (sender instanceof ConsoleCommandSender ?
                        ChatColor.WHITE + "[" + ChatColor.GREEN + "LC" + ChatColor.WHITE + "]" : "" + ChatColor.DARK_GRAY + ChatColor.BOLD + "\u24D8") + " "
                        + ChatColor.RESET + ChatColor.AQUA + s);
        return true;
    }

    public boolean info(@Nullable CommandSender sender, String s) {
        //sender = Util.def(sender, Bukkit.getConsoleSender());
        if (sender == null)
            sender = Bukkit.getConsoleSender();
        sender.sendMessage(
                (sender instanceof ConsoleCommandSender ?
                        ChatColor.WHITE + "[" + ChatColor.BLUE + "LC" + ChatColor.WHITE + "]" : "" + ChatColor.DARK_GRAY + ChatColor.BOLD + "\u24D8") + " "
                        + ChatColor.RESET + s);
        return true;
    }

    public boolean warn(@Nullable CommandSender sender, String s) {
        //sender = Util.def(sender, Bukkit.getConsoleSender());
        if (sender == null)
            sender = Bukkit.getConsoleSender();
        sender.sendMessage(
                (sender instanceof ConsoleCommandSender ?
                        ChatColor.WHITE + "[" + ChatColor.YELLOW + "LC" + ChatColor.WHITE + "]" : "" + ChatColor.GOLD + ChatColor.BOLD + "\u26A1") + " "
                + ChatColor.RESET + ChatColor.YELLOW + s);
        return true;
    }

    public boolean error(@Nullable CommandSender sender, String s) {
        //sender = Util.def(sender, Bukkit.getConsoleSender());
        if (sender == null)
            sender = Bukkit.getConsoleSender();
        sender.sendMessage(
                (sender instanceof ConsoleCommandSender ?
                        ChatColor.WHITE + "[" + ChatColor.RED + "LC" + ChatColor.WHITE + "]" : "" + ChatColor.DARK_RED + ChatColor.BOLD + "\u26A0") + " "
                + ChatColor.RESET + ChatColor.RED + s);
        return true;
    }

    public boolean severe(@Nullable CommandSender sender, String s) {
        //sender = Util.def(sender, Bukkit.getConsoleSender());
        if (sender == null)
            sender = Bukkit.getConsoleSender();
        sender.sendMessage(
                (sender instanceof ConsoleCommandSender ?
                        ChatColor.WHITE + "[" + ChatColor.DARK_RED + "LC" + ChatColor.WHITE + "]" : "" + ChatColor.DARK_RED + ChatColor.BOLD + "\u26A0") + " "
                        + ChatColor.RESET + ChatColor.DARK_RED + s);
        return true;
    }
}