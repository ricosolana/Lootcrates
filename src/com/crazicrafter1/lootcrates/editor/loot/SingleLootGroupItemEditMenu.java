package com.crazicrafter1.lootcrates.editor.loot;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.gapi.Component;
import com.crazicrafter1.gapi.RemovableComponent;
import com.crazicrafter1.gapi.SimplexMenu;
import com.crazicrafter1.gapi.TriggerComponent;
import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.lootcrates.crate.LootSet;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SingleLootGroupItemEditMenu extends SimplexMenu {

    public SingleLootGroupItemEditMenu(LootSet lootSet) {
        super("edit " + lootSet.id + " icon", 5, BACKGROUND_1);

        Component inputPerimeter = new Component() {
            @Override
            public ItemStack getIcon() {
                return new ItemBuilder(Material.RED_STAINED_GLASS).name("&eSet to").toItem();
            }
        };

        setComponent(1 + 3, 0, inputPerimeter);
        setComponent(3, 1, inputPerimeter);
        setComponent(2 + 3, 1, inputPerimeter);
        setComponent(1 + 3, 2, inputPerimeter);

        // Original crate
        setComponent(1, 1, new Component() {
            @Override
            public ItemStack getIcon() {
                return lootSet.itemStack;
            }
        });

        // Change to
        RemovableComponent rem = new RemovableComponent(null);
        setComponent(4, 1, rem);

        // Confirm
        setComponent(7, 1, new TriggerComponent() {
            @Override
            public void onLeftClick(Player p, boolean shift) {
                ItemStack item = rem.getIcon();
                if (item != null) {
                    Main.get().info("Applying changes here!");
                    lootSet.itemStack = new ItemBuilder(item.getType()).mergeLexicals(item).toItem();
                }
            }

            @Override
            public ItemStack getIcon() {
                return new ItemBuilder(Material.EMERALD_BLOCK).name("&6&lApply changes").toItem();
            }
        });

        // back
        backButton(4, 4, BACK_1, SingleLootGroupMenu.class, lootSet);
    }
}
