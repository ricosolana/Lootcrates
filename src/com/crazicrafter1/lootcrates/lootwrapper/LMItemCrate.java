package com.crazicrafter1.lootcrates.lootwrapper;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.gapi.Button;
import com.crazicrafter1.gapi.ParallaxMenu;
import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.lootcrates.crate.Crate;
import com.crazicrafter1.lootcrates.crate.LootSet;
import com.crazicrafter1.lootcrates.crate.loot.LootItemCrate;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class LMItemCrate extends LMWrapper {

    public LMItemCrate(LootItemCrate loot, LootSet lootSet) {
        menu = new ParallaxMenu.PBuilder()
                .title("LootItemCrate")
                .action(self -> {
                    for (Map.Entry<String, Crate> entry : Main.get().data.crates.entrySet()) {
                        Crate crate = entry.getValue();

                        Main.get().info("lootitemcrate: " + loot.id + ", iterate: " + crate.id);

                        ItemStack icon = new ItemBuilder(Material.LOOM).mergeLexicals(crate.itemStack).glow(crate.id.equals(loot.id)).toItem();

                        self.append(new Button.Builder()
                                .icon(icon)
                                .lmb(interact -> {
                                    // select as active
                                    loot.id = crate.id;
                                    //return Button.Result.refresh();
                                    return Button.Result.open(new LMItemCrate(loot, lootSet).menu);
                                })
                        );
                    }
                })
                .parentButton(4, 5)
                .validate();
    }
}
