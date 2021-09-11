package com.crazicrafter1.lootcrates.editor;

import com.crazicrafter1.gapi.ParallaxMenu;
import com.crazicrafter1.gapi.TriggerComponent;
import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.lootcrates.crate.Crate;
import com.crazicrafter1.lootcrates.crate.LootGroup;
import org.bukkit.entity.Player;

import java.util.Map;

public class LootMenu extends ParallaxMenu {

    public LootMenu() {
        super("&6Loot");

        for (Map.Entry<String, LootGroup> entry : Main.lootGroups.entrySet()) {

            addItem(new TriggerComponent(entry.getValue().itemStack()) {
                @Override
                public void onLeftClick(Player p) {

                }
            });
        }

    }
}
