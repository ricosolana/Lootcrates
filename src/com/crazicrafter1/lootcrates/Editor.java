package com.crazicrafter1.lootcrates;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.gapi.*;
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
                /*
                 * CrateList
                 */
                .childButton(1, 1, () -> new ItemBuilder(Material.CHEST).name("&3&lCrates").toItem(), new ParallaxMenu.PBuilder()
                        .title(ChatColor.DARK_GRAY + "Crates")
                        .action(self -> {
                            for (Map.Entry<String, Crate> entry : Main.get().data.crates.entrySet()) {
                                Crate crate = entry.getValue();
                                self.appendChild(() -> new ItemBuilder(crate.itemStack).lore("&8id: " + crate.id).toItem(), new SimpleMenu.SBuilder(5)
                                        .title("Crate: " + crate.id)
                                        .background()
                                        //.childButton(1, 1, new )
                                        .childButton(3, 1, () -> new ItemBuilder(Material.PAPER).name("&e&lChange Title").lore("&8Current: &r" + crate.title).toItem(), new TextMenu.TBuilder()
                                                .onComplete((player, s) -> EnumResult.BACK)
                                                .onClose(player -> EnumResult.BACK))
                                        .parentButton(4, 4)
                                        .validate()
                                );
                            }
                        })
                        .parentButton(4, 5)
                        .validate()
                /*
                 * LootList
                 */
                ).childButton(3, 1, () -> new ItemBuilder(Material.EXPERIENCE_BOTTLE).name("&6&lLoot").toItem(), new ParallaxMenu.PBuilder()
                        .title(ChatColor.GOLD + "Loot")
                        .action(self -> {
                            for (Map.Entry<String, LootSet> entry : Main.get().data.lootGroups.entrySet()) {
                                LootSet lootSet = entry.getValue();
                                self.appendChild(() -> new ItemBuilder(entry.getValue().itemStack).lore("&8id: " + entry.getKey() + "\n&8" + entry.getValue().loot.size() + " elements\n&8LMB: &2edit\n&8RMB: &cdelete").toItem(), new ParallaxMenu.PBuilder()
                                        .title("lootSet.id: " + lootSet.id)
                                        .action(c1_interact -> {
                                            for (AbstractLoot a : lootSet.loot) {
                                                Main.get().info("a: " + a);
                                                c1_interact.appendChild(() -> new ItemBuilder(a.getIcon()).lore(a + "\n&8LMB: &2edit\n&8RMB: &cdelete").toItem(),
                                                        LootCratesAPI.getWrapperMenu(p, a, lootSet, c1_interact)
                                                        //new Button.Builder()
                                                        //.icon()
                                                );
                                            }
                                        })
                                        .childButton(6, 5, () -> new ItemBuilder(Material.NETHER_STAR).name("&2New...").toItem(), new TextMenu.TBuilder()
                                                .onClose(player -> EnumResult.BACK)
                                                .onComplete((player, text) -> {
                                                    if (text.isEmpty()
                                                            || Main.get().data.lootGroups.containsKey(text)) {
                                                        return EnumResult.OK;
                                                    } else {
                                                        // add that lootgroup
                                                        Main.get().data.lootGroups.put(text,
                                                                new LootSet(text, new ItemStack(Material.RED_STAINED_GLASS),
                                                                        new ArrayList<>(List.of(new LootItem()))));

                                                        return EnumResult.OK;
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
                                            return EnumResult.OK;
                                        }
                                );

                            }
                        })
                        .parentButton(4, 5)
                        .validate()
                )
                //.childButton(5, 1, new ItemBuilder(Material.FIREWORK_ROCKET).name("&e&lFireworks").toItem(), new SimpleMenu.SBuilder()
                //        .)
                .validate().open(p);


    }

}
