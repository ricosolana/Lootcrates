package com.crazicrafter1.lootcrates.editor.loot.unique;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.gapi.*;
import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.lootcrates.crate.LootGroup;
import com.crazicrafter1.lootcrates.crate.loot.LootOrdinateItem;
import com.crazicrafter1.lootcrates.editor.loot.SingleAddLootMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class EditOrdinateItemMenu extends SimplexMenu {

    public EditOrdinateItemMenu(LootOrdinateItem loot, LootGroup lootGroup, Class<? extends Menu> prevMenuClass) {
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
        RemovableComponent rem = new RemovableComponent(null);
        setComponent(2, 1, rem);

        // Confirm
        setComponent(7, 1, new TriggerComponent() {
            @Override
            public void onLeftClick(Player p, boolean shift) {
                ItemStack item = rem.getIcon();
                if (item != null) {
                    Main.getInstance().info("Applying changes here!");

                    // add ordinate item with <item, min, max>
                    // somehow
                    lootGroup.loot.add(loot);
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
                    loot.min -= change;
                    new EditOrdinateItemMenu(loot, lootGroup, prevMenuClass).show(p);
                }
            }

            @Override
            public void onRightClick(Player p, boolean shift) {
                //INCREMENT
                int change = shift ? 5 : 1;
                if (loot.min+change <= loot.max) {
                    loot.min += change;
                    new EditOrdinateItemMenu(loot, lootGroup, prevMenuClass).show(p);
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
                    loot.max -= change;
                    new EditOrdinateItemMenu(loot, lootGroup, prevMenuClass).show(p);
                }
            }

            @Override
            public void onRightClick(Player p, boolean shift) {
                //INCREMENT
                int change = shift ? 5 : 1;
                if (loot.max+change <= 64) {
                    loot.max += change;
                    new EditOrdinateItemMenu(loot, lootGroup, prevMenuClass).show(p);
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
        backButton(4, 4, BACK_1, prevMenuClass, lootGroup);
    }
}
