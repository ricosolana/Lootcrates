package com.crazicrafter1.lootcrates.editor.misc;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.gapi.SimplexMenu;
import com.crazicrafter1.gapi.TriggerComponent;
import com.crazicrafter1.lootcrates.Data;
import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.lootcrates.editor.MainMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MiscMenu extends SimplexMenu {

    public MiscMenu() {
        super("&8Misc", 5, BACKGROUND_1);

        // toggle debug
        this.setComponent(1, 1, new TriggerComponent() {
            @Override
            public void onLeftClick(Player p) {
                Data.debug = !Data.debug;
                new MiscMenu().show(p);
            }

            @Override
            public ItemStack getIcon() {
                String s = Data.debug ? "&2enabled" : "&cdisabled";
                return new ItemBuilder(Material.COMMAND_BLOCK)
                        .name("&e&lToggle Debug")
                        .lore(s).toItem();
            }
        });

        // toggle update
        this.setComponent(4, 1, new TriggerComponent() {
            @Override
            public void onLeftClick(Player p) {
                Data.update = !Data.update;
                new MiscMenu().show(p);
            }

            @Override
            public ItemStack getIcon() {
                String s = Data.update ? "&2enabled" : "&cdisabled";
                return new ItemBuilder(Material.CLOCK)
                        .name("&b&lToggle Auto-Update")
                        .lore(s).toItem();
            }
        });

        // speed
        this.setComponent(7, 1, new TriggerComponent() {
            @Override
            public void onLeftClick(Player p) {
                // decrement
                if (Data.speed != 0) {
                    Data.speed--;
                    new MiscMenu().show(p);
                }
            }

            @Override
            public void onRightClick(Player p) {
                if (Data.speed != 20) {
                    Data.speed++;
                    new MiscMenu().show(p);
                }
            }

            @Override
            public ItemStack getIcon() {
                return new ItemBuilder(Material.FEATHER).count(Data.speed).name("&2&lReveal Speed")
                        .lore("""
                                 &8- left click to decrement
                                 &8- right click to increment
                                 &8Set to 0 to disable
                                """
                        ).toItem();
            }
        });

        // unselected item
        setComponent(2, 3, new TriggerComponent() {
            @Override
            public void onLeftClick(Player p) {

            }

            @Override
            public ItemStack getIcon() {
                return new ItemBuilder(Material.CHEST)
                        .name("Change Unselected Item").toItem();
            }
        });

        // selected item
        setComponent(6, 3, new TriggerComponent() {
            @Override
            public void onLeftClick(Player p) {

            }

            @Override
            public ItemStack getIcon() {
                return new ItemBuilder(Material.CHEST)
                        .name("Change Selected Item").toItem();
            }
        });


        this.backButton(4, 4, BACK_1, MainMenu.class);
    }

}
