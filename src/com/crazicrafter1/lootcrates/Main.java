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
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class Main extends JavaPlugin
{
    public static HashMap<java.util.UUID, ActiveCrate> openCrates = new HashMap<>();
    public static HashSet<UUID> crateFireWorks = new HashSet<>();

    public static boolean supportQualityArmory = false;
    //public static boolean supportGraphicalAPI = false;
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



    private static String temp_path;
    public Object a(String path, Object def) {
        temp_path = path;
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

        try {
            reloadConfigValues();
        } catch (Exception e) {
            error("Possible config issue around " + temp_path);
            e.printStackTrace();
        }

        if (Bukkit.getPluginManager().isPluginEnabled("GraphicalAPI"))
            editor = new ClickEditGUI();
        //try {
        //    if (autoUpdate)
        //        GithubUpdater.autoUpdate(this, "owner", "name", "resource");
        //} catch (Exception e) {
        //}

        //int pluginId = 10366; // <-- Replace with the id of your plugin!
        //Metrics metrics = new Metrics(this, pluginId);
        //// Optional: Add custom charts
        //metrics.addCustomChart(new Metrics.SimplePie("chart_id", () -> "My value"));


        //instance = this;

        String v = Bukkit.getVersion();

        //boolean valid = v.contains("1.14") || v.startsWith("1.15") || v.startsWith("1.16");

        new CmdCrates(this);
        new TabCrates(this);

        new ListenerOnDeath(this);
        new ListenerOnEntityDamageByEntity(this);
        new ListenerOnInventoryClick(this);
        new ListenerOnInventoryClose(this);
        new ListenerOnInventoryDrag(this);
        new ListenerOnPlayerInteract(this);
        new ListenerOnPlayerQuit(this);
        //new ListenerOnPortal(this);

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

    private boolean isOldConfigFormat() {

        //debug(config.getString("loot.weapon.items.item0.qa-item"));

        return config.contains("gui.selected.item");
    }

    @SuppressWarnings("unchecked")
    public void reloadConfigValues() {
        if (!new File(getDataFolder(), "config.yml").exists()) {
            saveDefaultConfig();
        }
        reloadConfig();

        boolean old = oldConfigFormat = isOldConfigFormat();

        if (old) info("Reading as old config format");
        else  info("Reading as new config format");

        debug = (boolean) a(old ? "debug-enabled" : "debug", true);

        selectionSound = Sound.valueOf((String) a("selection-sound", "ENTITY_EXPERIENCE_ORB_PICKUP"));
        inventoryName = ChatColor.translateAlternateColorCodes('&',(String) a("inventory-name", ""));
        inventorySize = (int) a("inventory-size", 36);
        raffleSpeed = (int) a("raffle-speed", 5);
        autoUpdate = (boolean) a("auto-update", true);

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

            List<String> _fade = (List<String>) a(old ? "firework.fade-colors" : "firework.fade", null);
            if (_fade != null)
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

        /*

                    ORDER SPECIFIC LOADING:

         */

        // 1st: store lootgroup ids
        for (String lootGroupKey : config.getConfigurationSection(old ? "gui.loot-group" : "gui.lootgroup").getKeys(false)) {
            lootGroups.put(lootGroupKey, null);
        }


        // 2nd: partially parse each crate
        for (String id : ((MemorySection)a("crates", null)).getKeys(false)) {
            //Crate crate = Crate.fromConfig(s);

            String path = "crates." + id;

            ItemBuilder builder = ItemBuilder.
                    builder(Material.matchMaterial((String) a(path + (old ? ".item" : ".icon"), null))).
                    name((String)a(path + (old ? ".name" : ".title"), null));

            {
                Object _footer = a(path + (old ? ".lore" : ".footer"), null);
                if (_footer != null)
                    builder.lore((List<String>)_footer);
            }
            Crate crate = new Crate(id, builder.toItem());

            debug("loadedCrate: " + id);

            Main.crates.put(id, crate);
            Main.crateNameIds.put(builder.toItem().getItemMeta().getDisplayName(), id);
        }

        // 3rd: load all lootgroups, using basic crate data if needed
        if (oldConfigFormat) {
            for (String lootGroupKey : config.getConfigurationSection("gui.loot-group").getKeys(false)) {
                LootGroup lootGroup = LootGroup.fromOldConfig(lootGroupKey);
                lootGroups.put(lootGroupKey, lootGroup);
            }
        } else {
            for (String lootGroupKey : config.getConfigurationSection("gui.lootgroup").getKeys(false)) {
                LootGroup lootGroup = LootGroup.fromNewConfig(lootGroupKey);
                lootGroups.put(lootGroupKey, lootGroup);
            }
        }


        // 4th: full parse crates
        for (String s : Main.crates.keySet()) {
            //Crate crate = Crate.fromConfig(s);
            MemorySection mem = (MemorySection) a("crates." + s + ".chances", null);

            Map<String, Integer> lootgroupChances = new HashMap<>();

            for (String group : mem.getKeys(false)) {
                int weight = mem.getInt(group);
                lootgroupChances.put(group, weight);
            }

            //= (Map<String, Integer>) config.get("crates." + s + ".chances");

            HashMap<LootGroup, Integer> lootGroups = new HashMap<>();
            for (String group : lootgroupChances.keySet()) {
                lootGroups.put(Main.lootGroups.get(group), lootgroupChances.get(group));
            }

            Main.crates.get(s).setLootGroups(lootGroups);
        }

        //if (supportGraphicalAPI)

    }

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