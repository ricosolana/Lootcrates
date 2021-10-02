package com.crazicrafter1.lootcrates.editor.crate;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.gapi.Main;
import com.crazicrafter1.gapi.ParallaxMenu;
import com.crazicrafter1.gapi.TriggerComponent;
import com.crazicrafter1.lootcrates.Data;
import com.crazicrafter1.lootcrates.crate.Crate;
import com.crazicrafter1.lootcrates.crate.LootGroup;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class SingleCrateLootMenu extends ParallaxMenu {

    public SingleCrateLootMenu(Crate crate, boolean editingWeights) {
        super("Loot ...");
        // on instantiate, draw

        Main.getInstance().info("" + editingWeights);

        /*
         * Depending on mode, either:
         * SELECT crate or CHANGE WEIGHT
         */
        for (LootGroup lootGroup : Data.lootGroups.values()) {
            addItem(new TriggerComponent() {
                @Override
                public void onLeftClick(Player p) {
                    if (editingWeights) {
                        // DECREMENT
                        if (crate.lootGroups.containsKey(lootGroup)) {
                            int weight = crate.lootGroups.get(lootGroup);

                            // Min clamp at 1
                            if (weight > 1) {
                                crate.lootGroups.put(lootGroup, weight - 1);
                            }
                        }
                    } else {
                        // TOGGLE
                        if (crate.lootGroups.containsKey(lootGroup)) {
                            crate.lootGroups.remove(lootGroup);
                        } else crate.lootGroups.put(lootGroup, 1);
                    }

                    // REFRESH
                    new SingleCrateLootMenu(crate, false).show(p);
                }

                @Override
                public void onRightClick(Player p) {
                    if (editingWeights) {
                        // increment

                        if (crate.lootGroups.containsKey(lootGroup)) {
                            int weight = crate.lootGroups.get(lootGroup);

                            // Weights of zero will never be fired off
                            if (weight < 64) {
                                crate.lootGroups.put(lootGroup, weight + 1);
                            }
                        }

                        //new SingleCrateLootMenu(crate, true).show(p);
                    } else {

                    }
                }

                @Override
                public ItemStack getIcon() {
                    if (crate.lootGroups.containsKey(lootGroup)) {
                        return new ItemBuilder(lootGroup.itemStack)
                                .glow(true).count(crate.lootGroups.get(lootGroup)).toItem();
                    } else
                        return new ItemBuilder(lootGroup.itemStack)
                                .glow(false).count(1).toItem();
                }
            });
        }

        // toggle mode
        //              8 doesn't work correctly
        setComponent(7, 5, new TriggerComponent() {
            @Override
            public void onLeftClick(Player p) {
                new SingleCrateLootMenu(crate, !editingWeights);
                //Main.getInstance().info("I was clicked!");
                Main.getInstance().info("p: " + editingWeights);
            }

            @Override
            public ItemStack getIcon() {
                //return new ItemBuilder(Material.FEATHER).name("sample item").toItem();
                if (editingWeights) {
                    return new ItemBuilder(Material.TARGET).name("&2Change mode").toItem();
                } else
                    return new ItemBuilder(Material.ANVIL).name("&2Change mode").toItem();
            }
        });

        backButton(4, 5, BACK_1, SingleCrateMenu.class, crate);
    }

    // now somehow gather every crate

}
