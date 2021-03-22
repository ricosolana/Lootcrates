package com.crazicrafter1.lootcrates;

import com.crazicrafter1.guiapi.GraphicalAPI;
import com.crazicrafter1.guiapi.MenuElement;
import com.crazicrafter1.guiapi.TemplateMenu;
import com.crazicrafter1.lootcrates.crate.Crate;
import com.crazicrafter1.lootcrates.crate.LootGroup;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class ClickEditGUI {

    // need to redo GraphicalGUI so that nested menus are able to be easily created
    public final TemplateMenu MAIN_MENU;

    private final TemplateMenu CRATE_MENU;
    private final TemplateMenu LOOTGROUP_MENU;
    private final TemplateMenu MISC_MENU;

    private final TemplateMenu SUB_CRATE_MENU;

    private final ItemStack CRATE_MENU_ICON = ItemBuilder.builder(Material.CHEST).name("&8Crates").toItem();
    private final ItemStack LOOT_MENU_ICON = ItemBuilder.builder(Material.ENCHANTED_GOLDEN_APPLE).name("&6Lootgroups").toItem();
    private final ItemStack MISC_MENU_ICON = ItemBuilder.builder(Material.FIREWORK_ROCKET).name("&8Misc").toItem();

    private final ItemStack NEXT_ICON = ItemBuilder.builder(Material.OAK_SIGN).name("&2Back").toItem();

    //private static TemplateMenu

    private static int clamp(int i, int a, int b) {
        return i < a ? a : i > b ? b : i;
    }

    public ClickEditGUI() {
        // menu init
        ItemStack background = ItemBuilder.builder(Material.BLACK_STAINED_GLASS_PANE).name(" ").toItem();
        MAIN_MENU = new TemplateMenu(ChatColor.DARK_GRAY + "LootCrates",
                1,
                new MenuElement(background));

        CRATE_MENU = new TemplateMenu(ChatColor.DARK_GREEN + "LootCrates -> Crates",
                clamp((Main.crates.size()/9)+1, 1, 6),
                new MenuElement(background));

        LOOTGROUP_MENU = new TemplateMenu(ChatColor.DARK_GREEN + "LootCrates -> Lootgroups",
                clamp((Main.lootGroups.size()/9)+1, 1, 6),
                new MenuElement(background));

        MISC_MENU = new TemplateMenu(ChatColor.DARK_GREEN + "LootCrates -> Misc",
                1,
                new MenuElement(background));

        SUB_CRATE_MENU = new TemplateMenu("",
                1,
                new MenuElement(background));

        /*
            MAIN MENU INIT
         */
        MAIN_MENU.addElement(new MenuElement(CRATE_MENU_ICON) {
            @Override
            public void onLeftClick(Player p) {
                // open crate menu
                GraphicalAPI.openMenu(p, CRATE_MENU);
            }
        }, 2, 0);

        MAIN_MENU.addElement(new MenuElement(LOOT_MENU_ICON) {
            @Override
            public void onLeftClick(Player p) {
                // open crate menu
                GraphicalAPI.openMenu(p, LOOTGROUP_MENU);
            }
        }, 4, 0);

        MAIN_MENU.addElement(new MenuElement(MISC_MENU_ICON) {
            @Override
            public void onLeftClick(Player p) {
                // open crate menu
                GraphicalAPI.openMenu(p, MISC_MENU);
            }
        }, 6, 0);

        // when the menu is opened, add
        //MAIN_MENU.onOpen(new Runnable() {
        //    @Override
        //    public void run() {
        //
        //    }
        //});

        // if at any point editor is used to add items or crates, change the menus from there

        /*
            add all crates to crates menu
         */
        int pitch = 0;
        for (Crate crate : Main.crates.values()) {
            if (pitch == 54) break;
            CRATE_MENU.addElement(new MenuElement(crate.getPreppedItemStack(false)) {
                @Override
                public void onLeftClick(Player p) {
                    // when crate clicked, open its data things
                    SUB_CRATE_MENU.setTitle(ChatColor.DARK_GREEN + "LootCrates -> Crates -> " + crate.getId());

                    SUB_CRATE_MENU.setHeight(
                            clamp((crate.getLootGroups().size()/9)+1, 1, 6));

                    SUB_CRATE_MENU.clear();

                    // add all loot
                    int i = 0;
                    for (Map.Entry<LootGroup, Integer> entry : crate.getLootGroups().entrySet()) {

                        ItemStack itemStack = ItemBuilder.builder(
                                entry.getKey().getPanel()).lore(
                                        new String[] {"" + entry.getValue()}).toItem();



                        SUB_CRATE_MENU.addElement(new MenuElement(itemStack), i++);
                    }

                    // open menu
                    GraphicalAPI.openMenu(p, SUB_CRATE_MENU);
                }
            }, pitch++);
        }

        pitch = 0;
        for (LootGroup lootGroup : Main.lootGroups.values()) {
            if (pitch == 54) break;
            LOOTGROUP_MENU.addElement(new MenuElement(lootGroup.getPanel()) {
                @Override
                public void onLeftClick(Player p) {
                    // do nothing yet
                }
            }, pitch++);
        }



    }

}
