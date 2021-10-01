package com.crazicrafter1.lootcrates.editor;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.crutils.Util;
import com.crazicrafter1.gapi.SimplexMenu;
import com.crazicrafter1.gapi.TriggerComponent;
import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.lootcrates.editor.crate.CrateMenu;
import com.crazicrafter1.lootcrates.editor.fireworks.FireworksMenu;
import com.crazicrafter1.lootcrates.editor.loot.LootMenu;
import com.crazicrafter1.lootcrates.editor.misc.MiscMenu;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainMenu extends SimplexMenu {



    public MainMenu() {
        super(ChatColor.DARK_GRAY + "LootCrates", 5, BACKGROUND_1);

        // Crates
        setComponent(1, 1, new TriggerComponent() {
            @Override
            public void onLeftClick(Player p) {
                // open another menu
                new CrateMenu().show(p);
            }

            @Override
            public ItemStack getIcon() {
                return new ItemBuilder(Material.CHEST).name("&3&lCrates").toItem();
            }
        });

        // LootGroup
        setComponent(3, 1, new TriggerComponent() {
            @Override
            public void onLeftClick(Player p) {
                new LootMenu().show(p);
            }

            @Override
            public ItemStack getIcon() {
                return new ItemBuilder(Material.EXPERIENCE_BOTTLE).name("&6&lLoot").toItem();
            }
        });

        // Fireworks
        setComponent(5, 1, new TriggerComponent() {
            @Override
            public void onLeftClick(Player p) {
                new FireworksMenu().show(p);
            }

            @Override
            public ItemStack getIcon() {
                return new ItemBuilder(Material.FIREWORK_ROCKET).name("&e&lFireworks").toItem();
            }
        });

        // Misc
        setComponent(7, 1, new TriggerComponent() {
            @Override
            public void onLeftClick(Player p) {
                new MiscMenu().show(p);
            }

            @Override
            public ItemStack getIcon() {
                return new ItemBuilder(Material.NETHERITE_SCRAP).name("&8&lMisc").toItem();
            }
        });

        // save config to disk
        setComponent(4, 3, new TriggerComponent() {
            @Override
            public void onLeftClick(Player p) {
                // configs / backups
                File configFile = new File(Main.getInstance().getDataFolder(), "config.yml");
                File backupFile = new File(Main.getInstance().getDataFolder(),"backup/" + System.currentTimeMillis() + "_config.yml");

                // create the backup path
                new File(Main.getInstance().getDataFolder(),"backup/").mkdirs();

                try {
                    // try to create the backup
                    backupFile.createNewFile();

                    // copy the old to the new
                    Util.copy(new FileInputStream(configFile), new FileOutputStream(backupFile));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Main.getInstance().saveConfig();
            }

            @Override
            public ItemStack getIcon() {
                return new ItemBuilder(Material.MAP).name("&6&lSave config to disk").toItem();
            }
        });

        //this.onClose();
    }
}
