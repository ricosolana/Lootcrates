package com.crazicrafter1.lootcrates.editor.loot;

import com.crazicrafter1.gapi.ParallaxMenu;
import com.crazicrafter1.gapi.TriggerComponent;
import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.lootcrates.crate.LootGroup;
import com.crazicrafter1.lootcrates.editor.MainMenu;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class LootMenu extends ParallaxMenu {

    public LootMenu() {
        super("&6Loot");

        for (Map.Entry<String, LootGroup> entry : Main.lootGroups.entrySet()) {

            addItem(new TriggerComponent() {
                @Override
                public void onLeftClick(Player p) {

                }

                @Override
                public ItemStack getIcon() {
                    return entry.getValue().itemStack();
                }
            });
        }

        backButton(4, 5, BACK_1, MainMenu.class);
    }
}
