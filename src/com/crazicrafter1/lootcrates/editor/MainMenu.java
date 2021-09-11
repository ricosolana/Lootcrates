package com.crazicrafter1.lootcrates.editor;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.gapi.Component;
import com.crazicrafter1.gapi.SimplexMenu;
import com.crazicrafter1.gapi.TriggerComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MainMenu extends SimplexMenu {

    public MainMenu() {
        super(ChatColor.DARK_GRAY + "LootCrates",
                3, new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name(" ").toItem());

        // Crates
        setComponent(1, 1,
                new TriggerComponent(new ItemBuilder(Material.CHEST).name("&3&lCrates").toItem()) {
            @Override
            public void onLeftClick(Player p) {
                // open another menu
                new CrateMenu().show(p);
            }
        });

        // LootGroup
        setComponent(3, 1,
                new TriggerComponent(new ItemBuilder(Material.EXPERIENCE_BOTTLE).name("&6&lLoot").toItem()) {
            @Override
            public void onLeftClick(Player p) {
                new LootMenu().show(p);
            }
        });

        // Fireworks
        setComponent(5, 1,
                new TriggerComponent(new ItemBuilder(Material.FIREWORK_ROCKET).name("&e&lFireworks").toItem()) {
            @Override
            public void onLeftClick(Player p) {

            }
        });

        // Misc
        setComponent(7, 1,
                new TriggerComponent(new ItemBuilder(Material.NETHERITE_SCRAP).name("&8&lMisc").toItem()) {
            @Override
            public void onLeftClick(Player p) {

            }
        });
    }
}
