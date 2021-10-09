package com.crazicrafter1.lootcrates.editor.loot;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.gapi.ParallaxMenu;
import com.crazicrafter1.gapi.TriggerComponent;
import com.crazicrafter1.gapi.anvil.AnvilGUI;
import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.lootcrates.crate.Crate;
import com.crazicrafter1.lootcrates.crate.LootSet;
import com.crazicrafter1.lootcrates.crate.loot.LootItem;
import com.crazicrafter1.lootcrates.editor.MainMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LootMenu extends ParallaxMenu {

    public LootMenu() {
        super("&6Loot");

        for (Map.Entry<String, LootSet> entry : Main.get().data.lootGroups.entrySet()) {
            addItem(new TriggerComponent() {
                @Override
                public void onRightClick(Player p, boolean shift) {
                    // delete from Data.lootgroups
                    Main.get().data.lootGroups.remove(entry.getKey());

                    // also remove from all Data.crates
                    for (Crate crate : Main.get().data.crates.values()) {
                        Integer removed = crate.lootByWeight.remove(entry.getValue());
                        if (removed != null)
                            crate.weightsToSums();
                    }

                    // refresh
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
                            .lore("&8id: " + entry.getKey() + "\n&8" + entry.getValue().loot.size() + " elements\n&8LMB: &2edit\n&8RMB: &cdelete").toItem();
                }
            });
        }


        /**
         * TODO
         * 'Create new LootGroup' button
         */

        setComponent(6, 5, new TriggerComponent() {
            @Override
            public void onLeftClick(Player p, boolean shift) {
                // add a new blank group???
                // must set the name
                // use anvil input somehow...
                // .. ffejmfjsdkf

                // 1. TAKE INPUT FOR LOOTGROUP NAME
                // 2. take user input easily ...

                //p.closeInventory();

                new AnvilGUI.Builder()
                    .onClose(player -> new LootMenu().show(p))
                    .itemLeft(new ItemStack(Material.IRON_AXE))
                    .title("New lootgroup")
                    .onComplete((player, text) -> {
                        // invalid
                        //Main.get().info(text);
                        if (text.isEmpty()
                                || Main.get().data.lootGroups.containsKey(text)) {
                            return AnvilGUI.Response.text("Invalid.");
                        } else {
                            // add that lootgroup
                            Main.get().data.lootGroups.put(text,
                                    new LootSet(text, new ItemStack(Material.RED_STAINED_GLASS),
                                            new ArrayList<>(List.of(new LootItem()))));



                            return AnvilGUI.Response.close();
                        }
                    })
                    .plugin(Main.get())
                    .open(p);
            }

            @Override
            public ItemStack getIcon() {
                return new ItemBuilder(Material.NETHER_STAR).name("&2New...").toItem();
            }
        });


        backButton(4, 5, BACK_1, MainMenu.class);
    }
}
