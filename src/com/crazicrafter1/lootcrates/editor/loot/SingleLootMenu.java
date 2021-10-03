package com.crazicrafter1.lootcrates.editor.loot;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.gapi.ParallaxMenu;
import com.crazicrafter1.gapi.RemovableComponent;
import com.crazicrafter1.gapi.TriggerComponent;
import com.crazicrafter1.lootcrates.crate.AbstractLoot;
import com.crazicrafter1.lootcrates.crate.LootGroup;
import com.crazicrafter1.lootcrates.crate.loot.*;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SingleLootMenu extends ParallaxMenu {

    //private boolean byIcon = true;

    public SingleLootMenu(LootGroup lootGroup) {
        super("Loot " + lootGroup.name);

        // list all loots
        for (AbstractLoot a : lootGroup.loot) {
            addItem(new TriggerComponent() {
                @Override
                public ItemStack getIcon() {
                    return new ItemBuilder(a.getIcon()).lore(a.toString()).toItem();
                }
            });
        }

        setComponent(6, 5, new TriggerComponent() {
            @Override
            public void onLeftClick(Player p, boolean shift) {
                // when clicked, open a new menu which goes over serializable types that
                // are valid
                new SingleAddLootMenu(lootGroup).show(p);
            }

            @Override
            public ItemStack getIcon() {
                return new ItemBuilder(Material.SPYGLASS).name("&2Add...").toItem();
            }
        });

        backButton(4, 5, BACK_1, LootMenu.class);
    }
}
