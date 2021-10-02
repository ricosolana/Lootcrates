package com.crazicrafter1.lootcrates.editor.crate;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.gapi.ParallaxMenu;
import com.crazicrafter1.gapi.TriggerComponent;
import com.crazicrafter1.lootcrates.Data;
import com.crazicrafter1.lootcrates.crate.Crate;
import com.crazicrafter1.lootcrates.crate.LootGroup;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SingleCrateLootMenu extends ParallaxMenu {

    enum Mode {
        WEIGHT_EDIT,
        SELECT_EDIT
    }

    public SingleCrateLootMenu(Crate crate, Mode mode) {
        super("Loot mode : " + mode.name());
        // on instantiate, draw

        /*
         * Depending on mode, either:
         * SELECT crate or CHANGE WEIGHT
         */
        for (LootGroup lootGroup : Data.lootGroups.values()) {
            addItem(new TriggerComponent() {
                @Override
                public void onLeftClick(Player p) {
                    if (mode == Mode.WEIGHT_EDIT) {
                        // DECREMENT
                        if (crate.lootGroupsByWeight.containsKey(lootGroup)) {
                            int weight = crate.lootGroupsByWeight.get(lootGroup);

                            // Min clamp at 1
                            if (weight > 1) {
                                crate.lootGroupsByWeight.put(lootGroup, weight - 1);
                                // reweigh
                                crate.weightsToSums();
                            }
                        }
                    } else {
                        // TOGGLE
                        if (crate.lootGroupsByWeight.containsKey(lootGroup)) {
                            crate.lootGroupsByWeight.remove(lootGroup);
                        } else crate.lootGroupsByWeight.put(lootGroup, 1);
                        crate.weightsToSums();
                    }

                    // REFRESH
                    new SingleCrateLootMenu(crate, mode).show(p);
                }

                @Override
                public void onRightClick(Player p) {
                    if (mode == Mode.WEIGHT_EDIT) {
                        // increment
                        if (crate.lootGroupsByWeight.containsKey(lootGroup)) {
                            int weight = crate.lootGroupsByWeight.get(lootGroup);

                            // Weights of zero will never be fired off
                            if (weight < 64) {
                                crate.lootGroupsByWeight.put(lootGroup, weight + 1);
                                crate.weightsToSums();
                            }
                        }

                        //new SingleCrateLootMenu(crate, true).show(p);
                    } else {
                        // no action
                    }

                    // REFRESH
                    new SingleCrateLootMenu(crate, mode).show(p);
                }

                @Override
                public ItemStack getIcon() {
                    if (crate.lootGroupsByWeight.containsKey(lootGroup)) {
                        return new ItemBuilder(lootGroup.itemStack)
                                .glow(true).count(crate.lootGroupsByWeight.get(lootGroup))
                                .lore(crate.getFormattedPercent(lootGroup)).toItem();
                    } else
                        return new ItemBuilder(lootGroup.itemStack)
                                .glow(false).count(1).toItem();
                }
            });
        }

        // toggle mode
        //              8 doesn't work correctly for some reason
        setComponent(7, 5, new TriggerComponent() {
            @Override
            public void onLeftClick(Player p) {

                new SingleCrateLootMenu(crate, mode == Mode.WEIGHT_EDIT ? Mode.SELECT_EDIT : Mode.WEIGHT_EDIT).show(p);
                //Main.getInstance().info("I was clicked!");
                //Main.getInstance().info("p: " + editingWeights);
            }

            @Override
            public ItemStack getIcon() {
                //return new ItemBuilder(Material.FEATHER).name("sample item").toItem();
                if (mode == Mode.WEIGHT_EDIT) {
                    return new ItemBuilder(Material.TARGET).name("&6Change mode").toItem();
                } else
                    return new ItemBuilder(Material.ANVIL).name("&8Change mode").toItem();
            }
        });

        backButton(4, 5, BACK_1, SingleCrateMenu.class, crate);
    }

    // now somehow gather every crate

}
