package com.crazicrafter1.lootcrates;

import java.io.File;
import java.io.IOException;
import java.util.*;

import com.crazicrafter1.crutils.*;
import com.crazicrafter1.lootcrates.crate.Crate;
import com.crazicrafter1.lootcrates.crate.ActiveCrate;
import com.crazicrafter1.lootcrates.crate.LootGroup;
import com.crazicrafter1.lootcrates.tabs.TabCrates;
import com.crazicrafter1.lootcrates.commands.CmdCrates;
import com.crazicrafter1.lootcrates.listeners.*;
import org.bukkit.*;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class Main extends JavaPlugin
{
    public static HashMap<java.util.UUID, ActiveCrate> openCrates = new HashMap<>();
    public static HashSet<UUID> crateFireworks = new HashSet<>();

    public static boolean supportQualityArmory = false;
    public static boolean supportGapi = false;
    public static boolean debug = false;
    public static boolean update = false;
    public static int speed;
    public static boolean seasonal = false;

    public static ItemStack unSelectedItem = null;
    public static ItemStack selectedItem = null;
    public static boolean enableFirework = false;
    public static FireworkEffect fireworkEffect = null;

    public static HashMap<String, Crate> crates = new HashMap<>();
    public static HashMap<String, LootGroup> lootGroups = new HashMap<>();

    VersionChecker updater = new VersionChecker(this, 68424);

    private FileConfiguration config;
    private File configFile;

    public String prefix = ChatColor.GRAY + "[" + ChatColor.AQUA + ChatColor.BOLD + "LootCrates" + ChatColor.GRAY + "] ";

    private static Main main;
    public static Main getInstance() {
        return main;
    }


    private boolean saveTheConfig = false;
    private static String lastPath;

    /**
     * Retrieve with defaults set
     * @param path key
     * @param def default
     * @return value
     */
    public Object a(String path, Object def) {
        lastPath = path;
        if (getConfig().contains(path)) {
            return getConfig().get(path);
        }
        info("Setting default for " + path + ": " + def.toString());
        getConfig().set(path, def);
        saveTheConfig = true;
        return def;
    }

    /**
     * Retrieve without defaults set
     * @param path key
     * @param def default
     * @return value
     */
    public Object b(String path, Object def) {
        lastPath = path;
        if (getConfig().contains(path)) {
            return getConfig().get(path);
        }
        return def;
    }

    @Override
    public void onEnable() {

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

        Main.main = this;
        if(!this.getDataFolder().exists()){
            this.getDataFolder().mkdirs();
        }

        supportQualityArmory = Bukkit.getPluginManager().isPluginEnabled("QualityArmory");



        reloadConfigValues();

        if (!update) {
            try {
                if (updater.hasNewUpdate()) {
                    important("New update : " + updater.getLatestVersion() + ChatColor.DARK_BLUE + " (" + updater.getResourceURL() + ")");

                } else {
                    info("LootCrates is up-to-date!");
                }

            } catch (Exception e) {
                error("An error occurred while checking for updates");
                if (debug)
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
                    () -> "" + Main.update));

            info("Metrics was successfully enabled");
        } catch (Exception e) {
            error("An error occurred while enabling metrics");
            if (debug)
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

        new BukkitRunnable() {
            @Override
            public void run() {
                if (Main.seasonal)
                    for (Crate crate : crates.values()) {
                        crate.prepSeasonalVariant();
                    }
            }
        }.runTaskTimer(this, 0, 20 * 60 * 60 * 6);

    }

    @SuppressWarnings("unchecked")
    public void reloadConfigValues() {
        try {
            if (!new File(getDataFolder(), "config.yml").exists()) {
                saveDefaultConfig();
            }
            reloadConfig();

            crates.clear();
            lootGroups.clear();

            /*
             * Config reading
             */
            debug = (boolean) a("debug", false);
            update = (boolean) a("update", true);
            // <defaults>
            String header = ChatColor.translateAlternateColorCodes('&', (String) a("header", "opening crate"));
            int columns = (int) a("columns", 3);
            int picks = (int) a("picks", 4);
            String soundName = (String)
                    b("sound", null);
            // </defaults>
            speed = (int) b("speed", 4);
            seasonal = (boolean) b("seasonal", false);

            // assert size
            if (!(columns >= 1 && columns <= 6)) {
                columns = 27;
                error("invalid size [1,6]");
            }

            unSelectedItem = new ItemBuilder(
                    Material.matchMaterial((String) a("gui.unselected.icon", null)))
                    .name((String) a("gui.unselected.title", null))
                    .lore((List<String>) b("gui.unselected.footer", new ArrayList<String>()))
                    .glow((boolean) b("gui.unselected.glow", false)).toItem();

            selectedItem = new ItemBuilder(
                    Material.matchMaterial((String) a("gui.selected.icon", null)))
                    .name((String) a("gui.selected.title", null))
                    .lore((List<String>) b("gui.selected.footer", new ArrayList<String>()))
                    .glow((boolean) b("gui.selected.glow", false)).toItem();

            enableFirework = (boolean) a("firework.enabled", enableFirework);

            {
                ArrayList<Color> colors = new ArrayList<>(), fade = new ArrayList<>();
                boolean flicker;

                List<String> _colors = (List<String>) b("firework.colors", null);
                if (_colors != null)
                    for (String c : _colors) {
                        Color color = Util.matchColor(c);
                        colors.add(color);
                    }

                List<String> _fade = (List<String>) b("firework.fade", null);
                if (_fade != null)
                    for (String s : _fade) {
                        Color color = Util.matchColor(s);
                        fade.add(color);
                    }

                flicker = (boolean) b("firework.flicker", false);

                if (!fade.isEmpty())
                    fireworkEffect = FireworkEffect.builder().withColor(colors).flicker(flicker).with(FireworkEffect.Type.BURST).build();
                else
                    fireworkEffect = FireworkEffect.builder().withColor(colors).flicker(flicker).with(FireworkEffect.Type.BURST).withFade(fade).build();

            }

            /* *\  /* *\  /* *\  /* *\  /* *\  /* *\
            |                                      |
            |        ORDER SPECIFIC LOADING:       |
            |                                      |
            /* *\  /* *\  /* *\  /* *\  /* *\  /* */

            // 1st: store lootgroup ids
            for (String lootGroupKey : config.getConfigurationSection("gui.lootgroup").getKeys(false)) {
                lootGroups.put(lootGroupKey, null);
            }

            // 2nd: partially parse each crate
            for (String id : ((MemorySection) a("crates", null)).getKeys(false)) {
                String path = "crates." + id;

                ItemBuilder builder = new ItemBuilder(Material.matchMaterial((String) a(path + ".icon", null))).
                        name((String) a(path + ".title", null)).
                        lore((List<String>) b(path + ".footer", null)).
                        customModelData((Integer) b(path + ".model", null));
                //                      ^ ^ ^ ^ ^ Integer cast is intentional

                // Get this crates final size
                int currentSize = (int) b(path + ".columns", columns) * 9;

                Sound currentSound = null;
                if (soundName != null)
                    currentSound = Sound.valueOf(
                        (String) b(path + ".sound", soundName));

                Crate crate = new Crate(id, builder.toItem(),
                        (String) b(path + ".header", header),
                        currentSize,
                        (int) b(path + ".picks", picks),
                        currentSound);

                Main.crates.put(id, crate);
            }

            // 3rd: load all lootgroups, using basic crate data if needed
            for (String lootGroupKey : config.getConfigurationSection("gui.lootgroup").getKeys(false)) {
                LootGroup lootGroup = LootGroup.fromConfig(lootGroupKey);
                lootGroups.put(lootGroupKey, lootGroup);
            }

            // 4th: full parse crates
            for (String s : Main.crates.keySet()) {
                MemorySection mem = (MemorySection) a("crates." + s + ".chances", null);

                Map<String, Integer> lootgroupChances = new HashMap<>();

                for (String group : mem.getKeys(false)) {
                    int weight = mem.getInt(group);
                    lootgroupChances.put(group, weight);
                }

                HashMap<LootGroup, Integer> lootGroups = new HashMap<>();
                for (String group : lootgroupChances.keySet()) {
                    lootGroups.put(Main.lootGroups.get(group), lootgroupChances.get(group));
                }

                Main.crates.get(s).setLootGroups(lootGroups);
            }

            if (saveTheConfig)
                this.saveConfig();

        } catch (Exception e) {
            error("Config issue around " + lastPath);
            if (debug)
                e.printStackTrace();
        }

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
        if (debug)
            Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.GOLD + s);
    }

    @Override
    public void reloadConfig() {
        if (configFile == null) {
            configFile = new File(this.getDataFolder(), "config.yml");
            if (!this.getDataFolder().exists())
                this.getDataFolder().mkdirs();
            if (!configFile.exists()) {
                try {
                    configFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        config = CommentYamlConfiguration.loadConfiguration(configFile);
        //config = this.getConfig();
    }

    @Override
    public FileConfiguration getConfig() {
        if (this.config == null) {
            this.reloadConfig();
        }
        return this.config;
    }

    @Override
    public void saveConfig() {
        if (this.config == null) {
            this.reloadConfig();
        }
        try {
            Main.getInstance().info("Saving config");
            this.config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}