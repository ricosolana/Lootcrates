package com.crazicrafter1.lootcrates.editor.crate;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.gapi.ParallaxMenu;
import com.crazicrafter1.gapi.TriggerComponent;
import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.lootcrates.crate.Crate;
import com.crazicrafter1.lootcrates.editor.MainMenu;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class CrateMenu extends ParallaxMenu {

    public CrateMenu() {
        super("&8Crates");

        for (Map.Entry<String, Crate> entry : Main.get().data.crates.entrySet()) {

            Crate crate = entry.getValue();

            ItemStack itemStack = new ItemBuilder(crate.itemStack).resetLore().toItem();

            addItem(new TriggerComponent() {
                @Override
                public void onLeftClick(Player p, boolean shift) {
                    // when clicking on this specific crate
                    new SingleCrateMenu(crate).show(p);
                }

                @Override
                public ItemStack getIcon() {
                    return itemStack;
                }
            });
        }



        backButton(4, 5, BACK_1, MainMenu.class);
    }
}
