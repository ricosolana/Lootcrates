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
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Main extends JavaPlugin
{
    public static void main(String[] args) throws Exception{

        //String s = "https://minecraft-heads.com/custom-heads/798";
        String s = "https://minecraft-heads.com/scripts/api.php?cat=";

        while (true) {
            Scanner in = new Scanner(System.in);

            URL url = new URL(s + in.nextLine());
            URLConnection conn = url.openConnection();

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));

            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        }

         //Pattern VALID_PATTERN = Pattern.compile("(?=.*[a-z])[a-z_]+");
 //
         //String test1 = "_";
 //
         //System.out.println("Valid: " + VALID_PATTERN.matcher(test1).matches());
    }

    private final File configFile = new File(getDataFolder(), "config.yml");
    private final File playerFile = new File(getDataFolder(), "player_stats.yml");
    private final File backupPath = new File(getDataFolder(), "backup");
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
    public Lang lang;
    private final HashMap<UUID, PlayerStat> playerStats = new HashMap<>();

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
            error(ChatColor.RED + "LootCrates requires a server restart to use");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
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

        loadExternalLoots();

        reloadConfig();

        if (data == null) {
            return;
        }

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
        this.lang = new Lang();

        loadPlayerStats();

        try {
            info("Attempt 1: Loading config");
            saveDefaultConfig(false, false);
            config.load(configFile);
            data = (Data) config.get("data");
        } catch (Exception e) {
            error(e.getMessage());
            try {
                popup("Attempt 2: Loading default config");

                saveDefaultConfig(true, false);
                config.load(configFile);
                data = (Data) config.get("data");
            } catch (Exception e1) {
                error(e1.getMessage());
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
            lang.loadLanguageFiles();
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
        }

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