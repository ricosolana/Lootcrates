package com.crazicrafter1.lootcrates;

import com.crazicrafter1.guiapi.GraphicalAPI;
import com.crazicrafter1.guiapi.GriddedMenuElement;
import com.crazicrafter1.guiapi.MenuElement;
import com.crazicrafter1.guiapi.TemplateMenu;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ClickEditGUI {

    // need to redo GraphicalGUI so that nested menus are able to be easily created
    public final TemplateMenu MAIN_MENU;

    private final TemplateMenu CRATE_MENU;
    private final TemplateMenu LOOT_MENU;
    private final TemplateMenu MISC_MENU;

    private final ItemStack CRATE_MENU_ICON = ItemBuilder.builder(Material.CHEST).name("&8Crates").toItem();
    private final ItemStack LOOT_MENU_ICON = ItemBuilder.builder(Material.ENCHANTED_GOLDEN_APPLE).name("&6Loot").toItem();
    private final ItemStack MISC_MENU_ICON = ItemBuilder.builder(Material.FIREWORK_ROCKET).name("&8Misc").toItem();

    //private static TemplateMenu

    public ClickEditGUI() {
        // menu init
        MAIN_MENU = new TemplateMenu(ChatColor.DARK_GRAY + "Lootcrates", 1,
                new MenuElement(ItemBuilder.builder(Material.BLACK_STAINED_GLASS_PANE).name(" ").toItem()));

        CRATE_MENU = new TemplateMenu(ChatColor.DARK_GREEN + "Lootcrates > Crates", 1,
                new MenuElement(ItemBuilder.builder(Material.BLACK_STAINED_GLASS_PANE).name(" ").toItem()));

        LOOT_MENU = new TemplateMenu(ChatColor.DARK_GREEN + "Lootcrates > Loot", 1,
                new MenuElement(ItemBuilder.builder(Material.BLACK_STAINED_GLASS_PANE).name(" ").toItem()));

        MISC_MENU = new TemplateMenu(ChatColor.DARK_GREEN + "Lootcrates -> Misc", 1,
                new MenuElement(ItemBuilder.builder(Material.BLACK_STAINED_GLASS_PANE).name(" ").toItem()));

        // menu icon init
        MAIN_MENU.addGriddedElement(new GriddedMenuElement(CRATE_MENU_ICON, 2, 0) {
            @Override
            public void onLeftClick(Player p) {
                // open crate menu
                GraphicalAPI.openMenu(p, CRATE_MENU);
            }
        });

        MAIN_MENU.addGriddedElement(new GriddedMenuElement(LOOT_MENU_ICON, 4, 0) {
            @Override
            public void onLeftClick(Player p) {
                // open crate menu
                GraphicalAPI.openMenu(p, LOOT_MENU);
            }
        });

        MAIN_MENU.addGriddedElement(new GriddedMenuElement(MISC_MENU_ICON, 6, 0) {
            @Override
            public void onLeftClick(Player p) {
                // open crate menu
                GraphicalAPI.openMenu(p, MISC_MENU);
            }
        });

        // crate menu icon init

        //CRATE_EDITOR = new TemplateMenu(ChatColor.DARK_GRAY + "Lootcrates Editor", 1, );
    }

}
