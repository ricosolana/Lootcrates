package com.crazicrafter1.lootcrates.editor.crate;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.gapi.Component;
import com.crazicrafter1.gapi.ParallaxMenu;
import com.crazicrafter1.gapi.TriggerComponent;
import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.lootcrates.crate.Crate;
import com.crazicrafter1.lootcrates.crate.LootGroup;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class SingleCrateLootMenu extends ParallaxMenu {

    public SingleCrateLootMenu(Crate crate) {
        super("Loot ...");
        // on instantiate, draw

        for (Map.Entry<String, LootGroup> entry : Main.DAT.lootGroups.entrySet()) {
            super.addItem(
                    new TriggerComponent() {
                        @Override
                        public void onLeftClick(Player p) {
                            // do something on click, weight, ?
                            //config.set("");
                        }

                        @Override
                        public ItemStack getIcon() {
                            if (crate.lootGroups.containsKey(entry.getKey())) {
                                return new ItemBuilder(entry.getValue().itemStack()).glow(true).toItem();
                            }
                            return entry.getValue().itemStack();
                        }
                    });
        }

        //for (Map.Entry<LootGroup, Integer> entry : ) {
        //    super.addItem(
        //        new TriggerComponent() {
        //            @Override
        //            public void onLeftClick(Player p) {
        //                // do something on click, weight, ?

        //            }

        //            @Override
        //            public ItemStack getIcon() {
        //                return entry.getKey().itemStack();
        //            }
        //        });
        //}

        //remove lootgroup
        setComponent(5, 5, new TriggerComponent() {
            @Override
            public void onLeftClick(Player p) {
                // increment
                //config.set(path + "chances", );

            }

            @Override
            public ItemStack getIcon() {
                return new ItemBuilder(Material.RED_DYE).name("-").toItem();
            }
        });

        //add lootgroup
        setComponent(6, 5, new TriggerComponent() {
            @Override
            public void onLeftClick(Player p) {
                // increment
                //config.set(path + "chances", );

            }

            @Override
            public ItemStack getIcon() {
                return new ItemBuilder(Material.GREEN_DYE).name("+").toItem();
            }
        });

        backButton(3, 5, BACK_1, SingleCrateMenu.class, crate);
    }

    // now somehow gather every crate

}
