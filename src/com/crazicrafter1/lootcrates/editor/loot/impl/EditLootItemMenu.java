package com.crazicrafter1.lootcrates.editor.loot.impl;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.crutils.ReflectionUtil;
import com.crazicrafter1.gapi.*;
import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.lootcrates.crate.LootSet;
import com.crazicrafter1.lootcrates.crate.loot.LootItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class EditLootItemMenu extends SimplexMenu {

    public EditLootItemMenu(LootItem loot, LootSet lootSet, Class<? extends Menu> prevMenuClass) {
        super("Loot add: Item", 5, BACKGROUND_1);

        Component inputPerimeter = new Component() {
            @Override
            public ItemStack getIcon() {
                return new ItemBuilder(Material.RED_STAINED_GLASS).name("&eSet to").toItem();
            }
        };

        setComponent(1 + 1, 0, inputPerimeter);
        setComponent(1, 1, inputPerimeter);
        setComponent(2 + 1, 1, inputPerimeter);
        setComponent(1 + 1, 2, inputPerimeter);

        // Change to
        RemovableComponent rem = new RemovableComponent(loot.getIcon());
        setComponent(2, 1, rem);

        // Confirm
        setComponent(7, 1, new TriggerComponent() {
            @Override
            public void onLeftClick(Player p, boolean shift) {
                ItemStack item = rem.getIcon();
                if (item != null) {
                    Main.get().info("Applying changes here!");

                    // add ordinate item with <item, min, max>
                    // somehow
                    //lootGroup.loot.add(loot);
                    loot.itemStack = new ItemStack(item);

                    // then go back to prev menu
                    ((Menu) ReflectionUtil.invokeConstructor(prevMenuClass, lootSet)).show(p);
                }
            }

            @Override
            public ItemStack getIcon() {
                return new ItemBuilder(Material.EMERALD_BLOCK).name("&6&lApply changes").toItem();
            }
        });

        // MIN
        setComponent(1, 3, new TriggerComponent() {
            @Override
            public void onLeftClick(Player p, boolean shift) {
                // DECREMENT
                int change = shift ? 5 : 1;
                if (loot.min > change) {
                    // need to pass the rem icon if it is not null
                    //if (rem.getIcon() != null) {
                    //    loot.icon = rem.getIcon();
                    //}
                    loot.min -= change;
                    new EditLootItemMenu(loot, lootSet, prevMenuClass).show(p);
                }
            }

            @Override
            public void onRightClick(Player p, boolean shift) {
                //INCREMENT
                int change = shift ? 5 : 1;
                if (loot.min+change <= loot.max) {
                    //if (rem.getIcon() != null) {
                    //    loot.icon = rem.getIcon();
                    //}
                    loot.min += change;
                    new EditLootItemMenu(loot, lootSet, prevMenuClass).show(p);
                }
            }

            @Override
            public ItemStack getIcon() {
                return new ItemBuilder(Material.IRON_TRAPDOOR).count(loot.min)
                        .name("&aMin bound")
                        .lore("""
                                &8LMB: &c-   &8SHIFT: x5
                                &8RMB: &2+
                                """
                        ).toItem();
            }
        });

        // MAX
        setComponent(3, 3, new TriggerComponent() {
            @Override
            public void onLeftClick(Player p, boolean shift) {
                // DECREMENT
                int change = shift ? 5 : 1;
                if (loot.max >= loot.min + change) {
                    //if (rem.getIcon() != null) {
                    //    loot.icon = rem.getIcon();
                    //}
                    loot.max -= change;
                    new EditLootItemMenu(loot, lootSet, prevMenuClass).show(p);
                }
            }

            @Override
            public void onRightClick(Player p, boolean shift) {
                //INCREMENT
                int change = shift ? 5 : 1;
                if (loot.max+change <= 64) {
                    //if (rem.getIcon() != null) {
                    //    loot.icon = rem.getIcon();
                    //}
                    loot.max += change;
                    new EditLootItemMenu(loot, lootSet, prevMenuClass).show(p);
                }
            }

            @Override
            public ItemStack getIcon() {
                return new ItemBuilder(Material.IRON_DOOR).count(loot.max)
                        .name("&2Max bound")
                        .lore("""
                                &8LMB: &c-   &8SHIFT: x5
                                &8RMB: &2+
                                """
                        ).toItem();
            }
        });

        // back
        backButton(4, 4, BACK_1, prevMenuClass, lootSet);
    }
}
