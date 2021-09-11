package com.crazicrafter1.lootcrates.editor;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.gapi.ParallaxMenu;
import com.crazicrafter1.gapi.TriggerComponent;
import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.lootcrates.crate.Crate;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class CrateMenu extends ParallaxMenu {

    public CrateMenu() {
        super("&8Crates");

        for (Map.Entry<String, Crate> entry : Main.crates.entrySet()) {

            Crate crate = entry.getValue();

            ItemStack itemStack = new ItemBuilder(crate.getItemStack(1)).resetLore().toItem();

            addItem(new TriggerComponent(itemStack) {
                @Override
                public void onLeftClick(Player p) {
                    // when clicking on this specific crate
                    new SingleCrateMenu(crate).show(p);
                }
            });
        }

        backButton(MainMenu.class, 4, 5, new ItemBuilder(Material.ARROW).name("&cBack").toItem());
    }
}
