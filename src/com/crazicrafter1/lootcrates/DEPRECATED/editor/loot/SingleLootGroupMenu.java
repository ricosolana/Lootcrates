package com.crazicrafter1.lootcrates.editor.loot;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.gapi.ParallaxMenu;
import com.crazicrafter1.gapi.TriggerComponent;
import com.crazicrafter1.lootcrates.LootCratesAPI;
import com.crazicrafter1.lootcrates.crate.loot.AbstractLoot;
import com.crazicrafter1.lootcrates.crate.LootSet;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SingleLootGroupMenu extends ParallaxMenu {

    //private boolean byIcon = true;

    public SingleLootGroupMenu(LootSet lootSet) {
        super("Loot " + lootSet.id);

        // list all loots
        for (AbstractLoot a : lootSet.loot) {
            addItem(new TriggerComponent() {

                @Override
                public void onLeftClick(Player p, boolean shift) {
                    LootCratesAPI.invokeMenu(a, lootSet, p, SingleLootGroupMenu.class);
                }

                @Override
                public void onRightClick(Player p, boolean shift) {
                    // delete
                    if (!lootSet.loot.isEmpty()) {
                        lootSet.loot.remove(a);
                        new SingleLootGroupMenu(lootSet).show(p);
                    }
                }

                @Override
                public ItemStack getIcon() {
                    return new ItemBuilder(a.getIcon()).lore(a +
                            "\n&8LMB: &2edit\n&8RMB: &cdelete").toItem();
                }
            });
        }

        // Add AbstractLoot prompt
        setComponent(6, 5, new TriggerComponent() {
            @Override
            public void onLeftClick(Player p, boolean shift) {
                // when clicked, open a new menu which goes over serializable types that
                // are valid
                new SingleAddTypeMenu(lootSet).show(p);
            }

            @Override
            public ItemStack getIcon() {
                return new ItemBuilder(Material.SPYGLASS).name("&aAdd...").toItem();
            }
        });

        // Edit LootGroup icon
        setComponent(2, 5, new TriggerComponent() {
            @Override
            public void onLeftClick(Player p, boolean shift) {
                // when clicked, open a new menu which goes over serializable types that
                // are valid

                new SingleLootGroupItemEditMenu(lootSet).show(p);
            }

            @Override
            public ItemStack getIcon() {
                return new ItemBuilder(lootSet.itemStack).name("&6Edit item").resetLore().toItem();
            }
        });

        backButton(4, 5, BACK_1, LootMenu.class);
    }
}
