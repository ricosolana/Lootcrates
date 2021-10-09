package com.crazicrafter1.lootcrates;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.gapi.Button;
import com.crazicrafter1.gapi.ParallaxMenu;
import com.crazicrafter1.gapi.SimpleMenu;
import com.crazicrafter1.gapi.TextMenu;
import com.crazicrafter1.lootcrates.crate.Crate;
import com.crazicrafter1.lootcrates.crate.LootSet;
import com.crazicrafter1.lootcrates.crate.loot.AbstractLoot;
import com.crazicrafter1.lootcrates.crate.loot.LootItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Editor {

    public static void open(Player p) {
        new SimpleMenu.SBuilder(5)
                .title(ChatColor.DARK_GRAY + "LootCrates")
                .background()
                .childButton(1, 1, new ItemBuilder(Material.CHEST).name("&3&lCrates").toItem(), new ParallaxMenu.PBuilder()
                        .title(ChatColor.DARK_GRAY + "Crates")
                        //foreach idea:
                        .action(self -> {
                            /*
                             * CrateList
                             */
                            for (Map.Entry<String, Crate> entry : Main.get().data.crates.entrySet()) {
                                Crate crate = entry.getValue();
                                self.appendChild(new ItemBuilder(crate.itemStack).lore("&8id: " + crate.id).toItem(), new SimpleMenu.SBuilder(5)
                                        .title("Crate: " + crate.id)
                                        .background()
                                        //.childButton(1, 1, new )
                                        .childButton(3, 1, new ItemBuilder(Material.PAPER).name("&e&lChange Title").lore("&8Current: &r" + crate.title).toItem(), new TextMenu.TBuilder()
                                                .onComplete((player, s) -> Button.Result.back())
                                                .onClose(player -> Button.Result.back()))
                                        .parentButton(4, 4));
                            }
                        })
                        .parentButton(4, 5)
                        .validate()
                ).childButton(3, 1, new ItemBuilder(Material.EXPERIENCE_BOTTLE).name("&6&lLoot").toItem(), new ParallaxMenu.PBuilder()
                        .title(ChatColor.GOLD + "Loot")
                        .action(self -> {
                            for (Map.Entry<String, LootSet> entry : Main.get().data.lootGroups.entrySet()) {
                                LootSet lootSet = entry.getValue();
                                self.appendChild(new ItemBuilder(entry.getValue().itemStack).lore("&8id: " + entry.getKey() + "\n&8" + entry.getValue().loot.size() + " elements\n&8LMB: &2edit\n&8RMB: &cdelete").toItem(), new ParallaxMenu.PBuilder()
                                        .title("lootSet.id: " + lootSet.id)
                                        .action(c1_interact -> {
                                            for (AbstractLoot a : lootSet.loot) {
                                                Main.get().info("a: " + a);
                                                c1_interact.appendChild(new ItemBuilder(a.getIcon()).lore(a + "\n&8LMB: &2edit\n&8RMB: &cdelete").toItem(),
                                                        LootCratesAPI.getWrapperMenu(p, a, lootSet, c1_interact)
                                                        //new Button.Builder()
                                                        //.icon()
                                                );
                                            }
                                        })
                                        .childButton(6, 5, new ItemBuilder(Material.NETHER_STAR).name("&2New...").toItem(), new TextMenu.TBuilder()
                                                .onClose(player -> Button.Result.back())
                                                .onComplete((player, text) -> {
                                                    if (text.isEmpty()
                                                            || Main.get().data.lootGroups.containsKey(text)) {
                                                        return Button.Result.OK();
                                                    } else {
                                                        // add that lootgroup
                                                        Main.get().data.lootGroups.put(text,
                                                                new LootSet(text, new ItemStack(Material.RED_STAINED_GLASS),
                                                                        new ArrayList<>(List.of(new LootItem()))));

                                                        return Button.Result.back();
                                                    }
                                                })
                                        )
                                        .parentButton(4, 5)
                                        .validate(),
                                        // RMB event
                                        interact -> {
                                            Main.get().data.lootGroups.remove(entry.getKey());
                                            for (Crate crate : Main.get().data.crates.values()) {
                                                Integer removed = crate.lootByWeight.remove(entry.getValue());
                                                if (removed != null)
                                                    crate.weightsToSums();
                                            }
                                            return Button.Result.OK();
                                        }
                                );

                            }
                        })
                        .parentButton(4, 5)
                        .validate()
                ).validate().open(p);


    }

}
