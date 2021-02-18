package com.crazicrafter1.lootcrates;

import java.io.File;
import java.io.IOException;
import java.util.*;

import com.crazicrafter1.lootcrates.config.CommentYamlConfiguration;
import com.crazicrafter1.lootcrates.crate.Crate;
import com.crazicrafter1.lootcrates.crate.ActiveCrate;
import com.crazicrafter1.lootcrates.crate.LootGroup;
import com.crazicrafter1.lootcrates.tabcompleters.TabCrates;
import com.crazicrafter1.lootcrates.commands.CmdCrates;
import com.crazicrafter1.lootcrates.listeners.*;
import com.crazicrafter1.lootcrates.tracking.VersionChecker;
import me.zombie_striker.qg.GithubUpdater;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class Main extends JavaPlugin
{
    public static HashMap<java.util.UUID, ActiveCrate> openCrates = new HashMap<>();
    public static HashSet<Firework> crateFireWorks = new HashSet<>();

    public static boolean supportQualityArmory = false;
    public static boolean debug = false;
    public static boolean autoUpdate = true;
    public static String inventoryName = "";
    public static int inventorySize = 36;
    public static int selections = 4;
    public static int raffleSpeed = 5;
    public static Sound selectionSound = Sound.valueOf("ENTITY_EXPERIENCE_ORB_PICKUP");
    public static ItemStack unSelectedItem = null;
    public static ItemStack selectedItem = null;
    public static boolean enableFirework = true;
    public static FireworkEffect fireworkEffect = null;

    public static HashMap<String, Crate> crates = new HashMap<>();
    public static HashMap<String, String> crateNameIds = new HashMap<>();
    public static HashMap<String, LootGroup> lootGroups = new HashMap<>();

    //public ConfigWrapper configWrapper;
    private FileConfiguration config;
    private File configFile;

    public static boolean oldConfigFormat = false;

    public String prefix = ChatColor.GRAY + "[" + ChatColor.AQUA + ChatColor.BOLD + "LootCrates" + ChatColor.GRAY + "] ";

    public ClickEditGUI editor = null;

    private static Main main;
    public static Main getInstance() {
        return main;
    }



    public Object a(String path, Object def) {
        if (getConfig().contains(path)) {
            return getConfig().get(path);
        }
        getConfig().set(path, def);
        //saveTheConfig = true;
        return def;
    }

    @Override
    public void onEnable() {
        main = this;
        if(!this.getDataFolder().exists()){
            this.getDataFolder().mkdirs();
        }
        supportQualityArmory = Bukkit.getPluginManager().isPluginEnabled("QualityArmory");



        //https://www.spigotmc.org/resources/68424
        VersionChecker updater = new VersionChecker(this, 68424);

        try {
            if (updater.hasNewUpdate()) {
                important("New update : " + updater.getLatestVersion() + ChatColor.DARK_BLUE + " (" + updater.getResourceURL() + ")");
            } else {
                info("LootCrates is up-to-date!");
            }
        } catch (Exception e) {
            error("Unable to check for updates!");
        }

        reloadConfigValues();
        try {
            if (autoUpdate)
                GithubUpdater.autoUpdate(this, "owner", "name", "resource");
        } catch (Exception e) {
        }

        //int pluginId = 10366; // <-- Replace with the id of your plugin!
        //Metrics metrics = new Metrics(this, pluginId);
        //// Optional: Add custom charts
        //metrics.addCustomChart(new Metrics.SimplePie("chart_id", () -> "My value"));


        //instance = this;

        String v = Bukkit.getVersion();

        boolean valid = v.startsWith("1.14") || v.startsWith("1.15");

        if (Bukkit.getPluginManager().isPluginEnabled("GraphicalAPI") && valid)
            editor = new ClickEditGUI();

        new CmdCrates(this);
        new TabCrates(this);

        new ListenerOnDeath(this);
        new ListenerOnEntityDamageByEntity(this);
        new ListenerOnInventoryClick(this);
        new ListenerOnInventoryClose(this);
        new ListenerOnInventoryDrag(this);
        new ListenerOnPlayerInteract(this);
        new ListenerOnPlayerQuit(this);
        new ListenerOnPortal(this);

        new BukkitRunnable() {
            @Override
            public void run() {
                //debug("Checking the season...");
                for (Crate crate : crates.values()) {
                    crate.prepSeasonalVariant();
                }
            }
        }.runTaskTimer(this, 0, 20*60*60); // Checks every hour

        info("Everything was successfully loaded!");
    }

    boolean isOldConfigFormat() {

        ArrayList<String> keys = new ArrayList<>(config.getConfigurationSection("loot").getKeys(false));

        String obliqueKey = keys.get(0);

        return config.isSet("loot." + obliqueKey + ".items");
    }

    @SuppressWarnings("unchecked")
    public void reloadConfigValues() {
        if (!new File(getDataFolder(), "config.yml").exists()) {
            saveDefaultConfig();
            reloadConfig();
        }

        boolean old = oldConfigFormat = isOldConfigFormat();

        selectionSound = Sound.valueOf((String) a("selection-sound", "ENTITY_EXPERIENCE_ORB_PICKUP"));
        inventoryName = (String) a("inventory-name", "");
        inventorySize = (int) a("inventory-size", 36);
        raffleSpeed = (int) a("raffle-speed", 5);
        autoUpdate = (boolean) a("auto-update", true);

        debug = (boolean) a(old ? "debug-enabled" : "debug", true);
        selections = (int) a(old ? "max-selections" : "selections", 4);

        unSelectedItem = ItemBuilder.builder(
                Material.matchMaterial((String) a(old ? "gui.unselected.item" : "gui.unselected.icon", null))).
                name((String) a(old ? "gui.unselected.name" : "gui.unselected.title", null))
                .lore((List<String>) a(old ? "gui.unselected.lore" : "gui.unselected.footer", new ArrayList<String>())).
                        glow((boolean) a("gui.unselected.glow", false)).toItem();

        selectedItem = ItemBuilder.builder(
                Material.matchMaterial((String) a(old ? "gui.selected.item" : "gui.selected.icon", null))).
                name((String) a(old ? "gui.selected.name" : "gui.selected.title", null))
                .lore((List<String>) a(old ? "gui.selected.lore" : "gui.selected.footer", new ArrayList<String>())).
                        glow((boolean) a("gui.selected.glow", false)).toItem();

        enableFirework = (boolean) a(old ? "firework-explosion" : "firework.enabled", true);

        /*
            TODO
            Loadfireworks
         */

        {
            //fireworkEffect =
            ArrayList<Color> colors = new ArrayList<>(), fade = new ArrayList<>();
            boolean flicker;

            List<String> _colors = (List<String>) a("firework.colors", null);
            if (_colors != null)
                for (String c : _colors) {
                    Color color = Util.matchColor(c);
                    colors.add(color);
                }

            List<String> _fade = (List<String>) a("firework.fade", null);
            for (String s : _fade) {
                Color color = Util.matchColor(s);
                fade.add(color);
            }


            flicker = (boolean) a("firework.flicker", false);

            if (!fade.isEmpty())
                fireworkEffect = FireworkEffect.builder().withColor(colors).flicker(flicker).with(FireworkEffect.Type.BURST).build();
            else
                fireworkEffect = FireworkEffect.builder().withColor(colors).flicker(flicker).with(FireworkEffect.Type.BURST).withFade(fade).build();

        }
        if (oldConfigFormat) {

            /*
                Load all LootGroups first, then load crates so that LootGroups can be referenced and not copied
             */



            // load crate data
            // iterate lootgroups
            for (String lootGroupKey : config.getConfigurationSection("gui.loot-group").getKeys(false)) {
                // iterate loot
                //LootGroup lootGroup = new LootGroup("gui.loot-group." + lootGroupKey);
            }

        }
    }

    //public static Main getInstance() { return instance; }

    public void info(String s) {
        Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.DARK_GRAY + s);
    }

    public void important(String s) {
        Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.DARK_PURPLE + s);
    }

    public void error(String s) {
        Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.DARK_RED + s);
    }

    public void debug(String s) {
        if (debug)
            Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.YELLOW + s);
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
		/*InputStream defConfigStream = this.getResource("config.yml");
		if (defConfigStream != null) {
			config.setDefaults(
					YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream, Charsets.UTF_8)));
		}*/
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
            this.config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}