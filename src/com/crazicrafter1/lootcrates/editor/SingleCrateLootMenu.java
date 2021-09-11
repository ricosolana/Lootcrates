package com.crazicrafter1.lootcrates.editor;

import com.crazicrafter1.gapi.Component;
import com.crazicrafter1.gapi.ParallaxMenu;
import com.crazicrafter1.gapi.TriggerComponent;
import com.crazicrafter1.lootcrates.crate.Crate;
import com.crazicrafter1.lootcrates.crate.LootGroup;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class SingleCrateLootMenu extends ParallaxMenu {

    public SingleCrateLootMenu(Crate crate) {
        super("Loot ...");
        // on instantiate, draw

        for (Map.Entry<LootGroup, Integer> entry : crate.getOriginalWeights().entrySet()) {
            super.addItem(
                new TriggerComponent(entry.getKey().itemStack()) {
                    @Override
                    public void onLeftClick(Player p) {
                        // do something on click, weight, ?

                    }
                });
        }
    }

    // now somehow gather every crate

}
