package com.crazicrafter1.lootcrates.editor.loot;

import com.crazicrafter1.crutils.ItemBuilder;
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

        for (Map.Entry<String, LootGroup> entry : Main.get().data.lootGroups.entrySet()) {
            addItem(new TriggerComponent() {
                @Override
                public void onRightClick(Player p, boolean shift) {
                    // delete
                    Main.get().data.lootGroups.remove(entry.getKey());
                    new LootMenu().show(p);
                }

                @Override
                public void onLeftClick(Player p, boolean shift) {
                    // open the loot
                    new SingleLootGroupMenu(entry.getValue()).show(p);
                }
                @Override
                public ItemStack getIcon() {
                    return new ItemBuilder(entry.getValue().itemStack)
                            .lore("&8" + entry.getValue().loot.size() + " elements\n&8LMB: &2edit\n&8RMB: &cdelete").toItem();
                }
            });
        }

        backButton(4, 5, BACK_1, MainMenu.class);
    }
}
