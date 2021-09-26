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
        super("Loot " + lootGroup.name());

        // list all loots
        for (AbstractLoot a : lootGroup.loot()) {
            addItem(new TriggerComponent() {
                @Override
                public ItemStack getIcon() {
                    return new ItemBuilder(a.getIcon()).lore(a.toString()).toItem();
                    //if (a instanceof LootCommand) {
                    //    return new ItemBuilder(a.getIcon()
                    //}else if (a instanceof LootItemCrate) {

                    //} else if (a instanceof LootItemEnchantable) {

                    //} else if (a instanceof LootItemPotion) {
                    //
                    //} else if (a instanceof LootItemQA) {
                    //
                    //} else if (a instanceof LootItem) {

                    //}
                }
            });
        }

        //this.setComponent(2, 5, new TriggerComponent() {
        //    @Override
        //    public void onLeftClick(Player p) {
        //        //
        //    }
        //});

        backButton(4, 5, BACK_1, LootMenu.class);
    }
}
