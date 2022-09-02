package com.crazicrafter1.lootcrates;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import com.crazicrafter1.crutils.*;
import com.crazicrafter1.lootcrates.cmd.Cmd;
import com.crazicrafter1.lootcrates.cmd.CmdTestParser;
import com.crazicrafter1.lootcrates.crate.Crate;
import com.crazicrafter1.lootcrates.crate.LootSet;
import com.crazicrafter1.lootcrates.crate.loot.*;
import com.crazicrafter1.lootcrates.listeners.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LCMain extends JavaPlugin
{
    public static final Pattern NUMBER_AT_END = Pattern.compile("\\d+$");
    private static final String SPLASH =
            " __         ______     ______     ______   ______     ______     ______     ______   ______     ______    \n" +
            "/\\ \\       /\\  __ \\   /\\  __ \\   /\\__  _\\ /\\  ___\\   /\\  == \\   /\\  __ \\   /\\__  _\\ /\\  ___\\   /\\  ___\\   \n" +
            "\\ \\ \\____  \\ \\ \\/\\ \\  \\ \\ \\/\\ \\  \\/_/\\ \\/ \\ \\ \\____  \\ \\  __<   \\ \\  __ \\  \\/_/\\ \\/ \\ \\  __\\   \\ \\___  \\  \n" +
            " \\ \\_____\\  \\ \\_____\\  \\ \\_____\\    \\ \\_\\  \\ \\_____\\  \\ \\_\\ \\_\\  \\ \\_\\ \\_\\    \\ \\_\\  \\ \\_____\\  \\/\\_____\\ \n" +
            "  \\/_____/   \\/_____/   \\/_____/     \\/_/   \\/_____/   \\/_/ /_/   \\/_/\\/_/     \\/_/   \\/_____/   \\/_____/";

    public Notifier notifier;

    public static final int REV_LATEST = 7;
    public static final String DISCORD_URL = "https://discord.gg/2JkFBnyvNQ";
    public static final String GITHUB_URL = "https://github.com/PeriodicSeizures/CRUtils/releases";
    public static final String PERM_ADMIN = "lootcrates.admin";
    public static final String PERM_OPEN = "lootcrates.open";
    public static final String PERM_PREVIEW = "lootcrates.preview";
    //private final File cratesConfigFile_REV8_ONWARD = new File(getDataFolder(), "rewards.yml");
    private final File rewardsConfigFile = new File(getDataFolder(), "rewards.yml");
    private final File configFile = new File(getDataFolder(), "config.yml");
    private final File backupPath = new File(getDataFolder(), "backup");

    public Map<Class<? extends ILoot>, ItemStack> lootClasses = new HashMap<>();

    private FileConfiguration config = null;
    private FileConfiguration rewardsConfig = null;

    public boolean supportQualityArmory = false;
    public boolean supportSkript = false;
    public boolean supportMMOItems = false;

    public SkriptAddon addon;

    public RewardSettings rewardSettings;
    public int rev = -1;
    public String language = "en";
    public boolean update = false;
    public int cleanPeriod = 30;
    public boolean debug = false;

    // -1 on indefinite revision
    // TODO remove rev
    @Deprecated
    private int findRev() {
        //noinspection ConstantConditions
        if (getDataFolder().listFiles().length == 0)
            return REV_LATEST;

        final File revFile = new File(getDataFolder(), "rev.yml");
        if (revFile.exists()) {
            FileConfiguration revConfig = new YamlConfiguration();
            try {
                revConfig.load(revFile);
                Files.delete(revFile.toPath());
                return revConfig.getInt("rev", REV_LATEST);
            } catch (Exception e) {
                notifier.severe(String.format(Lang.FAIL_REV_FILE, e.getMessage()));
                return -1;
            }
        }

        config = YamlConfiguration.loadConfiguration(configFile);
        int r = config.getInt("rev", -1);
        if (r != -1)
            return r;

        notifier.severe(Lang.UNKNOWN_REV);
        return -1;
    }

    private static LCMain instance;
    public static LCMain get() {
        return instance;
    }

    @Override
    public void onEnable() {
        LCMain.instance = this;

        notifier = new Notifier(ChatColor.WHITE + "[%sLC" + ChatColor.WHITE + "] %s%s", PERM_ADMIN);

        notifier.info(ColorUtil.renderAll(String.format(Lang.JOIN_DISCORD, DISCORD_URL)));

        try {
            Files.createDirectories(getDataFolder().toPath());
        } catch (IOException e) {
            notifier.warn(Lang.UNABLE_TO_CREATE);
            e.printStackTrace();
        }

        doSplash();

        reloadConfig();
        checkUpdates();
        checkAddons();
        reloadData(Bukkit.getConsoleSender()); // must be after skript
        initMetrics();

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
        this.saveConfig();
        this.saveOtherConfigs(Bukkit.getConsoleSender());
        deleteOldBackups(Bukkit.getConsoleSender());
    }



    @Override
    public void saveDefaultConfig() {
        saveDefaultConfig(Bukkit.getConsoleSender(), false);
    }

    @Override
    public void reloadConfig() {
        reloadConfig(Bukkit.getConsoleSender());
    }

    @Override
    public void saveConfig() {
        saveConfig(Bukkit.getConsoleSender());
    }



    private void registerLoot(@Nonnull Class<? extends ILoot> lootClass) {
        lootClasses.put(lootClass, (ItemStack) ReflectionUtil.getFieldInstance(ReflectionUtil.getField(lootClass, "EDITOR_ICON"), null));
        ConfigurationSerialization.registerClass(lootClass, lootClass.getSimpleName());
        LCMain.get().notifier.info("Registering " + lootClass.getSimpleName());
    }



    private void doSplash() {
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
    }



    private void checkUpdates() {
        boolean checkForUpdate = rev == -1 || !update;

        if (rev != -1) {
            if (update) try {
                StringBuilder outTag = new StringBuilder();
                if (GitUtils.updatePlugin(this, "PeriodicSeizures", "Lootcrates", "Lootcrates.jar", outTag)) {
                    notifier.warn(String.format(Lang.UPDATED, outTag));
                    notifier.warn(Lang.RECOMMEND_RESTART);
                } else {
                    notifier.info(Lang.LATEST_VERSION);
                }
            } catch (IOException e) {
                notifier.warn(Lang.UPDATE_FAIL);
                e.printStackTrace();
            }
        }

        if (checkForUpdate)
            GitUtils.checkForUpdateAsync(this, "PeriodicSeizures", "Lootcrates",
                    (result, tag) -> {
                        if (result) notifier.info(String.format(Lang.UPDATE_AVAILABLE, tag));
                        else notifier.info(Lang.LATEST_VERSION);
                    });
    }


    private void initMetrics() {
        try {
            Metrics metrics = new Metrics(this, 10395);

            metrics.addCustomChart(new Metrics.SimplePie("update",
                    () -> "" + update));

            metrics.addCustomChart(new Metrics.SimplePie("language",
                    () -> language));

            metrics.addCustomChart(
                    new Metrics.AdvancedPie("loot",
                            () -> rewardSettings.lootSets.keySet().stream().map(
                                    lootSet -> new AbstractMap.SimpleEntry<>(lootSet, 1)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
            );

            metrics.addCustomChart(
                    new Metrics.AdvancedPie("crates",
                            () -> rewardSettings.crates.keySet().stream().map(
                                    crate -> new AbstractMap.SimpleEntry<>(crate, 1)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
            );

        } catch (Exception e) {
            notifier.severe(String.format(Lang.UNABLE_TO_METRICS, e.getMessage()));
        }
    }



    private void checkAddons() {
        supportQualityArmory = Bukkit.getPluginManager().isPluginEnabled("QualityArmory");
        supportSkript = Bukkit.getPluginManager().isPluginEnabled("Skript");
        supportMMOItems = Bukkit.getPluginManager().isPluginEnabled("MMOItems");

        // Register serializable
        ConfigurationSerialization.registerClass(Data.class, "Data"); // TODO remove Data de/serializer
        ConfigurationSerialization.registerClass(LootSet.class, "LootSet");
        ConfigurationSerialization.registerClass(Crate.class, "Crate");

        // Register loot classes
        registerLoot(LootCommand.class);
        registerLoot(LootItem.class);
        registerLoot(LootItemCrate.class);
        registerLoot(LootNBTItem.class); // TODO remove rev
        if (supportQualityArmory) registerLoot(LootItemQA.class);
        if (supportSkript) {
            addon = Skript.registerAddon(this);
            try {
                addon.loadClasses(getClass().getPackage().getName(), "sk");
            } catch (Exception e) {
                notifier.severe(Lang.SKRIPT_INIT_ERROR);
                e.printStackTrace();
            }
            registerLoot(LootSkriptEvent.class);
        }
        if (supportMMOItems) registerLoot(LootMMOItem.class);
    }



    public void saveDefaultFile(CommandSender sender, File file, boolean replace) {
        if (replace || !Files.exists(file.toPath())) {
            notifier.info(sender, String.format(Lang.SAVING_DEFAULT, file.getName()));
            this.saveResource(file.getName(), true);
        }
    }

    public void saveDefaultConfig(CommandSender sender, boolean replace) {
        saveDefaultFile(sender, configFile, replace);
    }

    public boolean backupRewards(CommandSender sender, boolean isBroken) {
        File backupFile = new File(backupPath, System.currentTimeMillis() + "_" + (isBroken ? "broken" : "old") + "_rewards_rev" + rev + ".zip");

        notifier.info(sender, Lang.REWARDS_BACKUP);

        return Util.zip(rewardsConfigFile, backupFile);
    }



    public void reloadConfig(@Nonnull CommandSender sender) {
        try {
            saveDefaultConfig();

            this.rev = findRev(); // TODO remove rev

            // TODO remove rev
            if (rev <= 2) {
                // load several
                final File langFile = new File(getDataFolder(), "lang.yml");
                YamlConfiguration langConfig = YamlConfiguration.loadConfiguration(langFile);
                File noUpdateFile = new File(getDataFolder(), "NO_UPDATE.txt");

                this.language = langConfig.getString("language", "en");
                this.update = !(Files.exists(noUpdateFile.toPath()) && Files.isRegularFile(noUpdateFile.toPath()));

                Files.delete(langFile.toPath()); // throws if missing
                Files.deleteIfExists(noUpdateFile.toPath());
            } else if (rev == 3) {
                config = YamlConfiguration.loadConfiguration(configFile);

                this.language = config.getString("language", language);
                this.update = config.getBoolean("update", update);
                this.cleanPeriod = config.getInt("clean-after-days", cleanPeriod);
            } else if (rev <= 5) { // 4 and above
                config = YamlConfiguration.loadConfiguration(configFile);

                this.language = config.getString("language", language);
                this.update = config.getBoolean("update", update);
                this.cleanPeriod = config.getInt("clean-after-days", cleanPeriod);
            } else {
                config = YamlConfiguration.loadConfiguration(configFile);

                this.language = config.getString("language", language);
                this.update = config.getBoolean("update", update);
                this.cleanPeriod = config.getInt("clean-period", cleanPeriod);
                this.debug = config.getBoolean("debug", debug);
            }
        } catch (Exception e) {
            notifier.severe(sender, String.format(Lang.CONFIG_LOAD_FAIL, e.getMessage()));
        }
    }

    public void reloadData(@Nonnull CommandSender sender) {
        PlayerLog.loadAll(sender);

        // TODO remove rev
        if (rev <= 2) {
            // swap config.yml contents with rewards config in memory
            rewardsConfig = YamlConfiguration.loadConfiguration(configFile);
        } else {
            saveDefaultFile(sender, rewardsConfigFile, false);

            rewardsConfig = YamlConfiguration.loadConfiguration(rewardsConfigFile);
        }

        // save default en.yml file
        Lang.save(sender, "en", false);
        Lang.load(sender, language);

        // TODO remove rev
        if (rev >= 6) {
            try {
                this.rewardSettings = new RewardSettings(rewardsConfig);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        } else {
            try {
                notifier.info(sender, Lang.REWARDS_1);
                rewardSettings = Objects.requireNonNull((Data) rewardsConfig.get("data")).getSettings();
            } catch (Exception e) {
                //error(sender, e.getMessage());
                e.printStackTrace();
                try {
                    notifier.warn(sender, Lang.REWARDS_2);

                    saveDefaultFile(sender, rewardsConfigFile, true);
                    rewardsConfig.load(rewardsConfigFile);
                    rewardSettings = new RewardSettings(rewardsConfig);
                } catch (Exception e1) {
                    //error(sender, e1.getMessage());
                    e.printStackTrace();
                    try {
                        notifier.warn(sender, Lang.REWARDS_3);

                        rewardSettings = new RewardSettings();
                    } catch (Exception e2) {
                        // Very severe, should theoretically never reach this point
                        e2.printStackTrace();
                    }
                }
            }
        }

        if (rewardSettings != null) {
            notifier.info(sender, Lang.REWARDS_SUCCESS);
            return;
        }

        notifier.severe(sender, Lang.REWARDS_FAIL);
        notifier.severe(sender, String.format(Lang.REWARDS_REPORT, DISCORD_URL));
        Bukkit.getPluginManager().disablePlugin(this);
    }



    // TODO rev 8
    //   rev 8 will split the configs up more
    //      into crates.yml and rewards.yml
    //          crates.yml will contain the crates with their lootCollection id references
    //              rewards.yml will contain the lootItems in their respective lootCollections
    public void reloadCrateConfig(@Nonnull CommandSender sender) {
        PlayerLog.loadAll(sender);



        // TODO remove rev
        if (rev <= 2) {
            // swap config.yml contents with rewards config in memory
            rewardsConfig = YamlConfiguration.loadConfiguration(configFile);
        } else {
            saveDefaultFile(sender, rewardsConfigFile, false);

            rewardsConfig = YamlConfiguration.loadConfiguration(rewardsConfigFile);
        }

        // save default en.yml file
        Lang.save(sender, "en", false);
        Lang.load(sender, language);

        // TODO remove rev
        if (rev >= 6) {
            try {
                this.rewardSettings = new RewardSettings(rewardsConfig);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        } else {
            try {
                notifier.info(sender, Lang.REWARDS_1);
                rewardSettings = Objects.requireNonNull((Data) rewardsConfig.get("data")).getSettings();
            } catch (Exception e) {
                //error(sender, e.getMessage());
                e.printStackTrace();
                try {
                    notifier.warn(sender, Lang.REWARDS_2);

                    saveDefaultFile(sender, rewardsConfigFile, true);
                    rewardsConfig.load(rewardsConfigFile);
                    rewardSettings = new RewardSettings(rewardsConfig);
                } catch (Exception e1) {
                    //error(sender, e1.getMessage());
                    e.printStackTrace();
                    try {
                        notifier.warn(sender, Lang.REWARDS_3);

                        rewardSettings = new RewardSettings();
                    } catch (Exception e2) {
                        // Very severe, should theoretically never reach this point
                        e2.printStackTrace();
                    }
                }
            }
        }

        if (rewardSettings != null) {
            notifier.info(sender, Lang.REWARDS_SUCCESS);
            return;
        }

        notifier.severe(sender, Lang.REWARDS_FAIL);
        notifier.severe(sender, String.format(Lang.REWARDS_REPORT, DISCORD_URL));
        Bukkit.getPluginManager().disablePlugin(this);
    }



    public void saveConfig(@Nonnull CommandSender sender) {
        // if a backup was successfully made, then save

        if (rev == -1)
            return;

        try {
            config = new YamlConfiguration();

            config.set("rev", REV_LATEST);
            config.set("language", language);
            config.set("update", update);
            config.set("clean-period", cleanPeriod);
            config.set("debug", debug);

            config.save(configFile);
        } catch (IOException e) {
            notifier.severe(sender, String.format(Lang.CONFIG_SAVING_FAILED, e.getMessage()));
            e.printStackTrace();
        }
    }

    public void saveOtherConfigs(@Nonnull CommandSender sender) {
        // if a backup was successfully made, then save

        if (rev == -1)
            return;

        PlayerLog.saveAll(sender);

        if (backupRewards(sender, false)) {
            notifier.info(sender, Lang.CONFIG_Save);
            rewardsConfig = new YamlConfiguration();
            rewardSettings.serialize(rewardsConfig);

            try {
                rewardsConfig.save(rewardsConfigFile);
            } catch (Exception e) {
                notifier.severe(sender, Lang.CONFIG_SaveError);
                e.printStackTrace();
            }
        } else notifier.severe(sender, Lang.CONFIG_BackupError);
    }

    //private static final Pattern BACKUP_PATTERN = Pattern.compile("([0-9])+_\\S+_rewards_\\S+.zip");
    private static final Pattern BACKUP_PATTERN = Pattern.compile("^([0-9])+(_\\S+)?.zip");
    private void deleteOldBackups(@Nonnull CommandSender sender) {
        if (cleanPeriod <= 0)
            return;

        try {
            int deletedCount = 0;
            Files.createDirectories(backupPath.toPath());

            //noinspection ConstantConditions
            for (File file : backupPath.listFiles()) {
                String name = file.getName();
                Matcher matcher = BACKUP_PATTERN.matcher(name);
                if (matcher.matches()) {
                    long create = Long.parseLong(name.substring(0, name.indexOf("_")));
                    if (create < System.currentTimeMillis() - ((long)cleanPeriod * 24 * 60 * 60 * 1000)) {
                        // delete it
                        Files.delete(file.toPath());
                        deletedCount++;
                    }
                }
            }

            if (deletedCount > 0)
                notifier.info(sender, String.format(Lang.CONFIG_DELETES, deletedCount));
            else notifier.info(sender, Lang.NO_CONFIG_DELETES);
        } catch (Exception e) {
            notifier.severe(sender, String.format(Lang.CONFIG_DELETES_FAIL, e.getMessage()));
        }
    }





    @NotNull
    @Override
    public FileConfiguration getConfig() {
        if (this.config == null) {
            this.reloadConfig();
        }
        return this.config;
    }
}