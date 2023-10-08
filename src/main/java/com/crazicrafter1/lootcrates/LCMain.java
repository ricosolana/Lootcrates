package com.crazicrafter1.lootcrates;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import com.crazicrafter1.crutils.*;
import com.crazicrafter1.lootcrates.cmd.Cmd;
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
    public static Set<UUID> crateCerts = new HashSet<>(); // drm

    public Notifier notifier;

    public static final int REV_LATEST = 9;

    public static final String DISCORD_URL = "https://discord.gg/2JkFBnyvNQ";

    public static final String PERM_ADMIN = "lootcrates.admin";
    public static final String PERM_OPEN = "lootcrates.open";
    public static final String PERM_PREVIEW = "lootcrates.preview";

    public static final String KEY_rev = "rev";
    public static final String Key_language = "language";
    public static final String KEY_cleanPeriod = "clean-period";
    public static final String KEY_debug = "debug";
    public static final String KEY_checkCerts = "check-certs";
    public static final String KEY_certs = "certs";

    //private final File cratesConfigFile_REV9_ONWARD = new File(getDataFolder(), "crates.yml");
    private final File rewardsConfigFile = new File(getDataFolder(), "rewards.yml");
    //private final File lootConfigFile_REV9_ONWARD = new File(getDataFolder(), "loot.yml");
    private final File configFile = new File(getDataFolder(), "config.yml");
    private final File backupPath = new File(getDataFolder(), "backup");
    private final File certsFile = new File(getDataFolder(), "certs.yml");

    public Map<Class<? extends ILoot>, ItemStack> lootClasses = new HashMap<>();

    private FileConfiguration config = null;

    public boolean supportQualityArmory = false;
    public boolean supportSkript = false;
    public boolean supportMMOItems = false;

    public SkriptAddon addon;

    public RewardSettings rewardSettings;
    public int rev = REV_LATEST;
    public String language = "en";
    public int cleanPeriod = 30;
    public boolean debug = false;
    public boolean checkCerts = false;

    private static LCMain instance;
    public static LCMain get() {
        return instance;
    }

    @Override
    public void onEnable() {
        LCMain.instance = this;

        notifier = new Notifier(ChatColor.WHITE + "[%sLC" + ChatColor.WHITE + "] %s%s", PERM_ADMIN);

        notifier.info(ColorUtil.renderAll(String.format(Lang.MESSAGE_DISCORD, DISCORD_URL)));

        try {
            Files.createDirectories(getDataFolder().toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        doSplash();

        reloadConfig();
        checkUpdates();
        checkAddons();
        reloadData(Bukkit.getConsoleSender()); // must be after skript
        initMetrics();

        new Cmd(this);

        new ListenerOnEntityDamageByEntity(this);
        new ListenerCrateInteract(this);
        new ListenerOnInventoryClose(this);
        new ListenerDestroyCrate(this);
        new ListenerOnInventoryDrag(this);
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
        notifier.info("Registering " + lootClass.getSimpleName());
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
        GitUtils.checkForUpdateAsync(this, "PeriodicSeizures", "Lootcrates",
                (result, tag) -> {
                    if (result) notifier.info(String.format(Lang.MESSAGE_UPDATE_AVAILABLE, tag));
                    else notifier.info(Lang.MESSAGE_VERSION1);
                });
    }


    private void initMetrics() {
        try {
            Metrics metrics = new Metrics(this, 10395);

            metrics.addCustomChart(new Metrics.SimplePie(Key_language,
                    () -> language)
            );

            metrics.addCustomChart(new Metrics.AdvancedPie("loot",
                    () -> rewardSettings.lootSets.keySet().stream().map(
                            lootSet -> new AbstractMap.SimpleEntry<>(lootSet, 1)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
            );

            metrics.addCustomChart(
                    new Metrics.AdvancedPie("crates",
                            () -> rewardSettings.crates.keySet().stream().map(
                                    crate -> new AbstractMap.SimpleEntry<>(crate, 1)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
            );

            metrics.addCustomChart(
                new Metrics.SimplePie(KEY_checkCerts, () -> String.valueOf(checkCerts))
            );

        } catch (Exception e) {
            notifier.severe(String.format(Lang.METRICS_ERROR, e.getMessage()));
        }
    }



    private void checkAddons() {
        supportQualityArmory = Bukkit.getPluginManager().isPluginEnabled("QualityArmory");
        supportSkript = Bukkit.getPluginManager().isPluginEnabled("Skript");
        supportMMOItems = Bukkit.getPluginManager().isPluginEnabled("MMOItems");

        // Register loot classes
        registerLoot(LootCommand.class);
        registerLoot(LootItem.class);
        registerLoot(LootItemCrate.class);
        if (supportQualityArmory) registerLoot(LootItemQA.class);
        if (supportSkript) {
            addon = Skript.registerAddon(this);
            try {
                addon.loadClasses(getClass().getPackage().getName(), "sk");
            } catch (Exception e) {
                notifier.severe(Lang.MESSAGE_SKRIPT_ERROR1);
                e.printStackTrace();
            }
            registerLoot(LootSkriptEvent.class);
        }
        if (supportMMOItems) registerLoot(LootMMOItem.class);
    }



    public void saveDefaultFile(CommandSender sender, File file, boolean replace) {
        if (replace || !Files.exists(file.toPath())) {
            notifier.info(sender, String.format(Lang.MESSAGE_SAVING_DEFAULT, file.getName()));
            this.saveResource(file.getName(), true);
        }
    }

    public void saveDefaultConfig(CommandSender sender, boolean replace) {
        saveDefaultFile(sender, configFile, replace);
    }

    public boolean backupRewards(CommandSender sender, boolean isBroken) {
        File backupFile = new File(backupPath, System.currentTimeMillis() + "_" + (isBroken ? "broken" : "old") + "_rewards_rev" + rev + ".zip");

        notifier.info(sender, Lang.MESSAGE_REWARDS_BACKUP);

        return Util.zip(rewardsConfigFile, backupFile);
    }



    public void reloadConfig(@Nonnull CommandSender sender) {
        try {
            saveDefaultConfig();

            config = YamlConfiguration.loadConfiguration(configFile);

            this.rev = config.getInt(KEY_rev, REV_LATEST);
            this.language = config.getString(Key_language, language);
            this.cleanPeriod = config.getInt(KEY_cleanPeriod, cleanPeriod);
            this.debug = config.getBoolean(KEY_debug, debug);
            this.checkCerts = config.getBoolean(KEY_checkCerts, checkCerts);
        } catch (Exception e) {
            notifier.severe(sender, String.format(Lang.CONFIG_ERROR3, e.getMessage()));
        }
    }

    public void reloadData(@Nonnull CommandSender sender) {
        PlayerLog.loadAll(sender);

        // TODO close only open editor menus, not other possible ones
        //AbstractMenu.closeAllMenus();

        saveDefaultFile(sender, rewardsConfigFile, false);
        FileConfiguration rewardsConfig = YamlConfiguration.loadConfiguration(rewardsConfigFile);

        FileConfiguration certsConfig = YamlConfiguration.loadConfiguration(certsFile);
        crateCerts = certsConfig.getStringList(KEY_certs).stream().map(UUID::fromString).collect(Collectors.toSet());

        // save default en.yml file
        Lang.save(sender, "en", false);
        Lang.load(sender, language);

        try {
            this.rewardSettings = new RewardSettings(rewardsConfig);
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        if (rewardSettings != null) {
            notifier.info(sender, Lang.MESSAGE_REWARDS_SUCCESS);
            return;
        }

        notifier.severe(sender, String.format(Lang.MESSAGE_REWARDS_REPORT, DISCORD_URL));
        Bukkit.getPluginManager().disablePlugin(this);
    }


    /*
    // TODO rev 8
    //  rev 8 will have a crates.yml and rewards.yml
    //  crates.yml: contains crates with their lootCollection id references
    //  rewards.yml: contains lootItems in their respective lootCollections
    public void reloadCrateConfig(@Nonnull CommandSender sender) {
        PlayerLog.loadAll(sender);

        saveDefaultFile(sender, rewardsConfigFile, false);

        FileConfiguration rewardsConfig = YamlConfiguration.loadConfiguration(rewardsConfigFile);

        // save default en.yml file
        Lang.save(sender, "en", false);
        Lang.load(sender, language);

        try {
            this.rewardSettings = new RewardSettings(rewardsConfig);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (rewardSettings != null) {
            notifier.info(sender, Lang.REWARDS_SUCCESS);
            return;
        }

        notifier.severe(sender, Lang.REWARDS_FAIL);
        notifier.severe(sender, String.format(Lang.REWARDS_REPORT, DISCORD_URL));
        Bukkit.getPluginManager().disablePlugin(this);
    }
    */


    public void saveConfig(@Nonnull CommandSender sender) {
        // if a backup was successfully made, then save

        try {
            config = new YamlConfiguration();

            config.set(KEY_rev, REV_LATEST);
            config.set(Key_language, language);
            config.set(KEY_cleanPeriod, cleanPeriod);
            config.set(KEY_debug, debug);
            config.set(KEY_checkCerts, checkCerts);

            config.save(configFile);
        } catch (IOException e) {
            notifier.severe(sender, String.format(Lang.CONFIG_ERROR5, e.getMessage()));
            e.printStackTrace();
        }
    }

    public void saveOtherConfigs(@Nonnull CommandSender sender) {
        // if a backup was successfully made, then save

        PlayerLog.saveAll(sender);

        if (backupRewards(sender, false)) {
            notifier.info(sender, Lang.CONFIG_SAVING);

            FileConfiguration rewardsConfig = new YamlConfiguration();
            rewardSettings.serialize(rewardsConfig);

            FileConfiguration certsConfig = new YamlConfiguration();
            certsConfig.set(KEY_certs, crateCerts.stream().map(UUID::toString).collect(Collectors.toList()));

            try {
                rewardsConfig.save(rewardsConfigFile);
                certsConfig.save(certsFile);
            } catch (Exception e) {
                notifier.severe(sender, Lang.CONFIG_ERROR6);
                e.printStackTrace();
            }
        } else notifier.severe(sender, Lang.CONFIG_ERROR1);
    }

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
                notifier.info(sender, String.format(Lang.CONFIG_DELETED_OLD, deletedCount));
            else notifier.info(sender, Lang.MESSAGE_NO_CONFIG_DELETES);
        } catch (Exception e) {
            notifier.severe(sender, String.format(Lang.CONFIG_ERROR2, e.getMessage()));
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