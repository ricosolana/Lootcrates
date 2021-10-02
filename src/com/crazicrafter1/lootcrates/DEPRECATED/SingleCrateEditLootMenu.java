package com.crazicrafter1.lootcrates.DEPRECATED;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.gapi.ParallaxMenu;
import com.crazicrafter1.gapi.TriggerComponent;
import com.crazicrafter1.lootcrates.Data;
import com.crazicrafter1.lootcrates.crate.Crate;
import com.crazicrafter1.lootcrates.crate.LootGroup;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SingleCrateEditLootMenu extends ParallaxMenu {


    public SingleCrateEditLootMenu(Crate crate) {
        super("Select lootgroups");

        for (LootGroup lootGroup : Data.lootGroups.values()) {
            addItem(new TriggerComponent() {
                @Override
                public void onLeftClick(Player p) {
                    // toggle
                    if (crate.lootGroups.containsKey(lootGroup)) {
                        crate.lootGroups.remove(lootGroup);
                    } else crate.lootGroups.put(lootGroup, 1);
                    new SingleCrateEditLootMenu(crate).show(p);
                }

                @Override
                public ItemStack getIcon() {
                    return new ItemBuilder(lootGroup.itemStack)
                            .glow(crate.lootGroups.containsKey(lootGroup)).toItem();
                }
            });
        }
    }
}
