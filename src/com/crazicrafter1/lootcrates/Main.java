package com.crazicrafter1.lootcrates;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import com.crazicrafter1.innerutils.GithubInstaller;
import com.crazicrafter1.innerutils.GithubUpdater;
import com.crazicrafter1.innerutils.Metrics;
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
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Main extends JavaPlugin
{
    public static void main(String[] args) {
        Pattern VALID_PATTERN = Pattern.compile("(?=.*[a-z])[a-z_]+");

        String test1 = "_";

        System.out.println("Valid: " + VALID_PATTERN.matcher(test1).matches());
    }

    /*
     * Runtime modifiable stuff
     */
    public HashMap<UUID, ActiveCrate> openCrates = new HashMap<>();
    public HashSet<UUID> crateFireworks = new HashSet<>();
    public boolean supportQualityArmory = false;
    public boolean supportSkript = false;
    public boolean supportMMOItems = false;
    public boolean supportEcoItems = false;

    public SkriptAddon addon;

    public Data data;
    public Lang lang;
    private FileConfiguration config = null;
    private final File configFile = new File(getDataFolder(), "config.yml");
    private final File backupPath = new File(getDataFolder(), "backup");

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
            error(ChatColor.RED + "Must restart server to use plugin");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        Main.instance = this;

        supportQualityArmory = Bukkit.getPluginManager().isPluginEnabled("QualityArmory");
        supportSkript = Bukkit.getPluginManager().isPluginEnabled("Skript");
        supportMMOItems = Bukkit.getPluginManager().isPluginEnabled("MMOItems");
        supportEcoItems = Bukkit.getPluginManager().isPluginEnabled("EcoItems");

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

        if (supportEcoItems)
            LootCratesAPI.registerLoot(LootEcoItem.class);

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

            metrics.addCustomChart(new Metrics.SimplePie("loot",
                    () -> "" + LootCratesAPI.lootClasses.size()));

            metrics.addCustomChart(new Metrics.SimplePie("languages",
                    () -> "" + lang.translations.size()));

        } catch (Exception e) {
            error("Unable to enable bStats Metrics (" + e.getMessage() + ")");
        }

        /*
         * Command init
         */
        new Cmd(this);

        //MMOItems.plugin.getTiers().getAll().forEach(tier -> info("id: " + tier.getId() + ", name: " + tier.getName()));

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

    public Lang.Unit getLang(Player p) {

        // If translations are disabled return
        if (!data.lang)
            return null;

        Lang.Unit unit;

        String langCode = null;
        String locale = p.getLocale();
        int index = locale.indexOf("_");
        if (index != -1) {
            langCode = locale.toLowerCase(Locale.ROOT).substring(0, index);
        }

        // If player language is invalid, or
        // If player by default is using english, or
        // If language couldnt be found
        // return none
        if (langCode == null
                || langCode.equals("en")
                || (unit = lang.translations.get(langCode)) == null)
            return null;

        return unit;
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

                    LootCratesAPI.registerLoot(clazz);
                    info("Loaded external loot: " + clazz.getName() + " as " + alias);
                }
                reader.close();
            }
        } catch (Exception e) {e.printStackTrace();}
    }

    @Override
    public void onDisable() {
        if (instance == null)
            return;

        this.saveConfig();
    }

    public void saveDefaultConfig(boolean replace) {
        // If replacing, then save
        // If back up failed, then save
        if ((replace && backupConfig(replace)) || !configFile.exists()) {
            info("Saving default config");
            this.saveResource(configFile.getName(), true);
        }
    }

    @Override
    public void reloadConfig() {
        this.config = new YamlConfiguration();
        this.lang = new Lang();

        //saveLanguageFiles();

        int configAttempt = CFG_WAIT;
        while (++configAttempt != CFG_ERR) {
            try {
                switch (configAttempt) {
                    case CFG_CURR:
                        info("Attempt 1: Loading current or default config");

                        saveDefaultConfig(false);
                        config.load(configFile);
                        data = (Data) config.get("data");
                        lang.loadLanguageFiles();
                        return;
                    case CFG_DEF:
                        info("Attempt 2: Force loading default config");

                        saveDefaultConfig(true);
                        config.load(configFile);
                        data = (Data) config.get("data");
                        lang.loadLanguageFiles();
                        return;
                    case CFG_POP:
                        info("Attempt 3: Populating config with minimal built-ins");

                        data = new Data();
                        lang.loadLanguageFiles();
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
        File backupFile = new File(backupPath, System.currentTimeMillis() + "_" + (isBroken ? "broken" : "old") + "_config.zip");

        info("Backing up config");

        try {
            backupFile.getParentFile().mkdirs();

            ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(backupFile));
            zipOut.putNextEntry(new ZipEntry(configFile.getName()));

            byte[] bytes = config.saveToString().getBytes();
            zipOut.write(bytes, 0, bytes.length);

            zipOut.close();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public void saveConfig() {
        // if a backup was successfully made, then save

        lang.saveLanguageFiles();

        if (backupConfig(false)) {
            info("Saving config...");
            config.set("data", data);

            try {
                config.save(configFile);
            } catch (Exception e) {
                error("Failed to save config");
                e.printStackTrace();
            }
        }

        purge();
    }

    private static final Pattern BACKUP_PATTERN = Pattern.compile("([0-9])+_\\S+_config.zip");
    private void purge() {
        // now delete old files in backup
        // backup/1010928476782461_old_config.yml
        int deletedCount = 0;
        try {
            backupPath.mkdirs();

            //noinspection ConstantConditions
            for (File file : backupPath.listFiles()) {
                String name = file.getName();
                Matcher matcher = BACKUP_PATTERN.matcher(name);
                if (matcher.matches()) {
                    long create = Long.parseLong(name.substring(0, name.indexOf("_")));
                    if (create < System.currentTimeMillis() - (data.cleanHour * 60 * 60 * 1000)) {
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

    @Override
    public FileConfiguration getConfig() {
        if (this.config == null) {
            this.reloadConfig();
        }
        return this.config;
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

    public boolean info(CommandSender sender, String s) {
            sender.sendMessage(
                    (sender instanceof ConsoleCommandSender ? ChatColor.WHITE + "[" + ChatColor.DARK_PURPLE + "L" + ChatColor.LIGHT_PURPLE + "C" + ChatColor.WHITE + "]" : "" + ChatColor.DARK_GRAY + ChatColor.BOLD + "\u24D8") + " "
                    + ChatColor.RESET + ChatColor.GRAY + s);
        return true;
    }

    public boolean warn(CommandSender sender, String s) {
        sender.sendMessage(
                (sender instanceof ConsoleCommandSender ? ChatColor.WHITE + "[" + ChatColor.GOLD + "L" + ChatColor.YELLOW + "C" + ChatColor.WHITE + "]" : "" + ChatColor.GOLD + ChatColor.BOLD + "\u26A1") + " "
                + ChatColor.RESET + ChatColor.YELLOW + s);
        return true;
    }

    public boolean error(CommandSender sender, String s) {
        sender.sendMessage(
                (sender instanceof ConsoleCommandSender ? ChatColor.WHITE + "[" + ChatColor.DARK_RED + "L" + ChatColor.RED + "C" + ChatColor.WHITE + "]" : "" + ChatColor.DARK_RED + ChatColor.BOLD + "\u26A0") + " "
                + ChatColor.RESET + ChatColor.RED + s);
        return true;
    }
}