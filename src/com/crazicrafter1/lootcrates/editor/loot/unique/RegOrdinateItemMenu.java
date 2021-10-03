package com.crazicrafter1.lootcrates.editor.loot.unique;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.gapi.Component;
import com.crazicrafter1.gapi.RemovableComponent;
import com.crazicrafter1.gapi.SimplexMenu;
import com.crazicrafter1.gapi.TriggerComponent;
import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.lootcrates.crate.LootGroup;
import com.crazicrafter1.lootcrates.crate.loot.LootOrdinateItem;
import com.crazicrafter1.lootcrates.editor.crate.SingleCrateMenu;
import com.crazicrafter1.lootcrates.editor.loot.SingleAddLootMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class RegOrdinateItemMenu extends SimplexMenu {

    public RegOrdinateItemMenu(LootGroup lootGroup) {
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
                    //lootGroup.loot.add(new LootOrdinateItem(item));
                }
            }

            @Override
            public ItemStack getIcon() {
                return new ItemBuilder(Material.EMERALD_BLOCK).name("&6&lApply changes").toItem();
            }
        });

        // back
        backButton(4, 4, BACK_1, SingleAddLootMenu.class, lootGroup);
    }
}
