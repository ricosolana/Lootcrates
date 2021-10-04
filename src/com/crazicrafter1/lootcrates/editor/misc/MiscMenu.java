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

        Data data = Main.get().data;

        // toggle debug
        this.setComponent(1, 1, new TriggerComponent() {
            @Override
            public void onLeftClick(Player p, boolean shift) {
                data.debug = !data.debug;
                new MiscMenu().show(p);
            }

            @Override
            public ItemStack getIcon() {
                String s = data.debug ? "&2enabled" : "&cdisabled";
                return new ItemBuilder(Material.COMMAND_BLOCK)
                        .name("&e&lToggle Debug")
                        .lore(s).toItem();
            }
        });

        // toggle update
        this.setComponent(4, 1, new TriggerComponent() {
            @Override
            public void onLeftClick(Player p, boolean shift) {
                data.update = !data.update;
                new MiscMenu().show(p);
            }

            @Override
            public ItemStack getIcon() {
                String s = data.update ? "&2enabled" : "&cdisabled";
                return new ItemBuilder(Material.CLOCK)
                        .name("&b&lToggle Auto-Update")
                        .lore(s).toItem();
            }
        });

        // speed
        this.setComponent(7, 1, new TriggerComponent() {
            @Override
            public void onLeftClick(Player p, boolean shift) {
                // decrement
                if (data.speed != 0) {
                    data.speed--;
                    new MiscMenu().show(p);
                }
            }

            @Override
            public void onRightClick(Player p, boolean shift) {
                if (data.speed != 20) {
                    data.speed++;
                    new MiscMenu().show(p);
                }
            }

            @Override
            public ItemStack getIcon() {
                return new ItemBuilder(Material.FEATHER).count(data.speed)
                        .name("&2&lReveal Speed")
                        .lore("""
                                &8LMB: &c-
                                &8RMB: &2+
                                &8Setting to 0 will disable
                                """
                        ).toItem();
            }
        });

        // unselected item
        setComponent(2, 3, new TriggerComponent() {
            @Override
            public void onLeftClick(Player p, boolean shift) {

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
            public void onLeftClick(Player p, boolean shift) {

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
