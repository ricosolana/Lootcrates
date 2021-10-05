package com.crazicrafter1.lootcrates.editor.crate;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.crutils.Util;
import com.crazicrafter1.gapi.ParallaxMenu;
import com.crazicrafter1.gapi.TriggerComponent;
import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.lootcrates.crate.Crate;
import com.crazicrafter1.lootcrates.crate.LootSet;
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
        if (mode == Mode.SELECT_EDIT) {
            for (LootSet lootSet : Main.get().data.lootGroups.values()) {

                addItem(new TriggerComponent() {
                    @Override
                    public void onLeftClick(Player p, boolean shift) {
                        // TOGGLE
                        if (crate.lootByWeight.containsKey(lootSet)) {
                            crate.lootByWeight.remove(lootSet);
                        } else crate.lootByWeight.put(lootSet, 1);

                        crate.weightsToSums();

                        // REFRESH
                        new SingleCrateLootMenu(crate, mode).show(p);
                    }

                    @Override
                    public void onRightClick(Player p, boolean shift) {
                        // REFRESH
                        new SingleCrateLootMenu(crate, mode).show(p);
                    }

                    @Override
                    public ItemStack getIcon() {
                        if (crate.lootByWeight.containsKey(lootSet)) {
                            return new ItemBuilder(lootSet.itemStack)
                                    .glow(true)
                                    .lore("&8id: " + lootSet.id + "\n&aIncluded").toItem();
                        } else
                            return new ItemBuilder(lootSet.itemStack)
                                    .glow(false)
                                    .lore("&8id: " + lootSet.id + "\n&cExcluded").toItem();
                    }
                });
            }

        } else {


            for (LootSet lootSet : crate.lootByWeight.keySet()) {
                addItem(new TriggerComponent() {
                    @Override
                    public void onLeftClick(Player p, boolean shift) {
                        // DECREMENT
                        if (crate.lootByWeight.containsKey(lootSet)) {
                            int weight = crate.lootByWeight.get(lootSet);

                            final int change = shift ? 5 : 1;

                            // Min clamp at 1
                            if (weight > change) {
                                crate.lootByWeight.put(lootSet, weight - change);
                                // reweigh
                                crate.weightsToSums();
                            }
                        }

                        // REFRESH
                        new SingleCrateLootMenu(crate, mode).show(p);
                    }

                    @Override
                    public void onRightClick(Player p, boolean shift) {
                        // increment
                        if (crate.lootByWeight.containsKey(lootSet)) {
                            int weight = crate.lootByWeight.get(lootSet);

                            final int change = shift ? 5 : 1;

                            // Weights of zero will never be fired off
                            crate.lootByWeight.put(lootSet, weight + change);
                            crate.weightsToSums();

                        }

                        // REFRESH
                        new SingleCrateLootMenu(crate, mode).show(p);
                    }

                    @Override
                    public ItemStack getIcon() {
                        int count = crate.lootByWeight.get(lootSet);
                        String prob = String.format("&2%s  =  %s\n", crate.getFormattedFraction(lootSet), crate.getFormattedPercent(lootSet));
                        return new ItemBuilder(lootSet.itemStack)
                                .count(Util.clamp(count, 1, 64))
                                .lore(prob +    """
                                                &8LMB: &c-   &8SHIFT: x5
                                                &8RMB: &2+
                                                """
                                ).toItem();
                    }
                });
            }


        }




        // toggle mode
        //              x slot 8 and 0 do not work because of next/prev page
        setComponent(6, 5, new TriggerComponent() {
            @Override
            public void onLeftClick(Player p, boolean shift) {

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
