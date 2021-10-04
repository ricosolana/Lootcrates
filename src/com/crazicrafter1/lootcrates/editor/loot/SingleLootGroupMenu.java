package com.crazicrafter1.lootcrates.editor.loot;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.gapi.ParallaxMenu;
import com.crazicrafter1.gapi.TriggerComponent;
import com.crazicrafter1.lootcrates.LootCratesAPI;
import com.crazicrafter1.lootcrates.crate.AbstractLoot;
import com.crazicrafter1.lootcrates.crate.LootGroup;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SingleLootGroupMenu extends ParallaxMenu {

    //private boolean byIcon = true;

    public SingleLootGroupMenu(LootGroup lootGroup) {
        super("Loot " + lootGroup.name);

        // list all loots
        for (AbstractLoot a : lootGroup.loot) {
            addItem(new TriggerComponent() {

                @Override
                public void onLeftClick(Player p, boolean shift) {
                    LootCratesAPI.invokeMenu(a, lootGroup, p, SingleLootGroupMenu.class);
                }

                @Override
                public void onRightClick(Player p, boolean shift) {
                    // delete
                    if (!lootGroup.loot.isEmpty()) {
                        lootGroup.loot.remove(a);
                        new SingleLootGroupMenu(lootGroup).show(p);
                    }
                }

                @Override
                public ItemStack getIcon() {
                    return new ItemBuilder(a.getIcon()).lore(a +
                            "\n&8LMB: &2edit\n&8RMB: &cdelete").toItem();
                }
            });
        }

        setComponent(6, 5, new TriggerComponent() {
            @Override
            public void onLeftClick(Player p, boolean shift) {
                // when clicked, open a new menu which goes over serializable types that
                // are valid
                new SingleAddTypeMenu(lootGroup).show(p);
            }

            @Override
            public ItemStack getIcon() {
                return new ItemBuilder(Material.SPYGLASS).name("&2Add...").toItem();
            }
        });

        backButton(4, 5, BACK_1, LootMenu.class);
    }
}
