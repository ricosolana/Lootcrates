package com.crazicrafter1.lootcrates.config;

import com.crazicrafter1.lootcrates.Main;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;


public class ConfigMigrator {

    private YamlConfiguration newConfig;

    public ConfigMigrator(Main plugin, YamlConfiguration old_config) {
        // load config, test if it old version based on appearance
        //FileConfiguration config = plugin.getConfig();

        ArrayList<String> keys = new ArrayList<>(old_config.getConfigurationSection("loot").getKeys(false));

        String obliqueKey = keys.get(0);

        if (old_config.isSet("loot." + obliqueKey + ".items")) {

            // rename old config
            Path path_old = Paths.get(new File(plugin.getDataFolder(), "config.yml").toURI());
            Path path_new = Paths.get(new File(plugin.getDataFolder(), "config_old_" + System.currentTimeMillis() + ".yml").toURI());

            try {
                Files.move(path_old, path_old.resolveSibling(path_new));
            } catch (Exception e) {
                plugin.error("Couldn't rename old config for migration:");
                e.printStackTrace();
                Bukkit.getPluginManager().disablePlugin(plugin);
                return;
            }

            //newConfig = CommentYamlConfiguration.loadConfiguration(path_old.toFile());
            plugin.saveDefaultConfig();

            //
        }
    }
}
