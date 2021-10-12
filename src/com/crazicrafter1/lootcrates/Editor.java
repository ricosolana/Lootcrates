package com.crazicrafter1.lootcrates;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.crutils.ReflectionUtil;
import com.crazicrafter1.crutils.Util;
import com.crazicrafter1.gapi.*;
import com.crazicrafter1.lootcrates.crate.Crate;
import com.crazicrafter1.lootcrates.crate.LootSet;
import com.crazicrafter1.lootcrates.crate.loot.AbstractLoot;
import com.crazicrafter1.lootcrates.crate.loot.LootItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Editor {

    enum Mode {
        WEIGHT_EDIT,
        SELECT_EDIT
    }

    @SuppressWarnings("DanglingJavadoc")
    public static void open(Player p) {
        Button.Builder inOutline = new Button.Builder().icon(() -> new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name("&7Set to").toItem());
        new SimpleMenu.SBuilder(5)
                .title(ChatColor.DARK_GRAY + "LootCrates")
                .background()
                /*********************\
                 *                   *
                 * Global Crate List *
                 *                   *
                 \*******************/
                .childButton(1, 1, () -> new ItemBuilder(Material.CHEST).name("&3&lCrates").toItem(), new ParallaxMenu.PBuilder()
                        .title(ChatColor.DARK_GRAY + "Crates")
                        .parentButton(4, 5)
                        .action(self -> {
                            ArrayList<Button> result = new ArrayList<>();
                            for (Map.Entry<String, Crate> entry : Main.get().data.crates.entrySet()) {
                                Crate crate = entry.getValue();
                                result.add(new Button.Builder()
                                        .icon(() -> new ItemBuilder(crate.itemStack).lore("&8id: " + crate.id).toItem())
                                        .child(self, new SimpleMenu.SBuilder(5)
                                                .title("Crate: " + crate.id)
                                                .background()
                                                .parentButton(4, 4)
                                                // *   *   *
                                                // Edit Crate ItemStack
                                                // *   *   *
                                                .childButton(1, 1, () -> new ItemBuilder(crate.itemStack.getType()).name("&b&lEdit Item").resetLore().toItem(), new SimpleMenu.SBuilder(5)
                                                        .title("edit item")
                                                        .background()
                                                        .parentButton(4, 4)
                                                        .button(2, 1, inOutline)
                                                        .button(3, 2, inOutline)
                                                        .button(2, 3, inOutline)
                                                        .button(1, 2, inOutline)
                                                        // Edit ItemStack
                                                        .button(2, 2, new Button.Builder()
                                                                .icon(() -> crate.itemStack)
                                                                .lmb(interact -> {
                                                                    if (interact.heldItem != null)
                                                                        crate.itemStack = LootCratesAPI.makeCrate(new ItemBuilder(interact.heldItem).toItem(), crate.id);
                                                                    return EnumResult.GRAB_ITEM;
                                                                }))
                                                        // Edit Name
                                                        .childButton(6, 1, () -> new ItemBuilder(Material.NAME_TAG).name("&eName").lore("&8LMB: &aEdit").toItem(), new TextMenu.TBuilder()
                                                                .title("&8Edit name")
                                                                .leftInput(new Button.Builder().icon(() -> crate.itemStack))
                                                                .rightInput(new Button.Builder().icon(() -> new ItemBuilder(crate.itemStack.getType()).name("&8Use '&' for colors").lore("&7Ignore the &cX &7>").toItem()))
                                                                .onClose(player -> EnumResult.BACK)
                                                                .onComplete((player, s) -> {
                                                                    if (s.isEmpty())
                                                                        return EnumResult.OK;
                                                                    crate.itemStack = new ItemBuilder(crate.itemStack).name(s).toItem();
                                                                    return EnumResult.BACK;
                                                                }))
                                                        // Edit Lore
                                                        .childButton(6, 3, () -> new ItemBuilder(Material.MAP).name("&7Lore").lore("&8LMB: &aEdit").toItem(), new TextMenu.TBuilder()
                                                                .title("&8Item lore")
                                                                .leftInput(new Button.Builder().icon(() -> crate.itemStack))
                                                                .rightInput(new Button.Builder().icon(() -> new ItemBuilder(crate.itemStack.getType()).name("&8Use '&' for colors").lore("&7Ignore the &cX &7>").toItem()))
                                                                .onClose(player -> EnumResult.BACK)
                                                                .onComplete((player, s) -> {
                                                                    if (s.isEmpty())
                                                                        return EnumResult.OK;
                                                                    crate.itemStack = new ItemBuilder(crate.itemStack).lore(s).toItem();
                                                                    return EnumResult.BACK;
                                                                }))
                                                )
                                                // *   *   *
                                                // Edit Inventory Title
                                                // *   *   *
                                                .childButton(3, 1, () -> new ItemBuilder(Material.PAPER).name("&e&lTitle").lore("&8Current: " + crate.title + "\n&8LMB: &aEdit").toItem(), new TextMenu.TBuilder()
                                                        .title("edit title")
                                                        .text(Util.toAlternateColorCodes('&', crate.title))
                                                        .onComplete((player, s) -> {
                                                            // set name if it is not empty
                                                            if (!s.isEmpty()) {
                                                                crate.title = ChatColor.translateAlternateColorCodes('&', "&7" + s);
                                                                return EnumResult.BACK;
                                                            }

                                                            return EnumResult.OK;
                                                        })
                                                        .onClose(player -> EnumResult.BACK)
                                                )
                                                // *   *   *
                                                // Edit LootSets
                                                // *   *   *
                                                .childButton(5, 1, () -> new ItemBuilder(Material.ANVIL).name("&a&lEdit Loot").toItem(), new ParallaxMenu.PBuilder()
                                                        .title("Edit weights")
                                                        .parentButton(4, 5)
                                                        .action(builder -> {
                                                            ArrayList<Button> result1 = new ArrayList<>();

                                                            for (var lootSet : Main.get().data.lootSets.values()) {
                                                                Integer weight = crate.lootByWeight.get(lootSet);
                                                                Button.Builder btn = new Button.Builder();
                                                                ItemBuilder b = new ItemBuilder(lootSet.itemStack.getType()).name("&8" + lootSet.id);
                                                                if (weight != null) {
                                                                    b.lore("&7" + crate.getFormattedFraction(lootSet) +
                                                                            "\n&7" + crate.getFormattedPercent(lootSet) +
                                                                            "\n&fLMB: &c-\n&fRMB: &2+" +
                                                                            "\n&fMMB: &7toggle\n" +
                                                                            "&fShift: &7x5").glow(true);
                                                                    btn.mmb(interact -> {
                                                                        // toggle inclusion
                                                                        crate.lootByWeight.remove(lootSet);
                                                                        crate.weightsToSums();
                                                                        return EnumResult.REFRESH;
                                                                    }).lmb(interact -> {
                                                                        // decrement
                                                                        int change = interact.isShift() ? 5 : 1;

                                                                        crate.lootByWeight.put(lootSet, Util.clamp(weight - change, 1, Integer.MAX_VALUE));
                                                                        crate.weightsToSums();

                                                                        return EnumResult.REFRESH;
                                                                    }).rmb(interact -> {
                                                                        // decrement
                                                                        int change = interact.isShift() ? 5 : 1;

                                                                        crate.lootByWeight.put(lootSet, Util.clamp(weight + change, 1, Integer.MAX_VALUE));
                                                                        crate.weightsToSums();

                                                                        return EnumResult.REFRESH;
                                                                    });
                                                                } else {
                                                                    b.lore("&fMMB: &7toggle");
                                                                    btn.mmb(interact -> {
                                                                        crate.lootByWeight.put(lootSet, 1);
                                                                        crate.weightsToSums();
                                                                        return EnumResult.REFRESH;
                                                                    });
                                                                }
                                                                result1.add(btn.icon(b::toItem).get());
                                                            }

                                                            return result1;
                                                        })
                                                )
                                                // *   *   *
                                                // Edit Columns
                                                // *   *   *
                                                .button(7, 1, new Button.Builder()
                                                        .icon(() -> new ItemBuilder(Material.LADDER).name("&8&nColumns").lore("&7LMB: &c-\n&7RMB: &2+").toItem())
                                                        .lmb(interact -> {
                                                            // decrease
                                                            crate.columns = Util.clamp(crate.columns - 1, 1, 6);
                                                            return EnumResult.REFRESH;
                                                        })
                                                        .rmb(interact -> {
                                                            // decrease
                                                            crate.columns = Util.clamp(crate.columns + 1, 1, 6);
                                                            return EnumResult.REFRESH;
                                                        }))
                                                // *   *   *
                                                // Edit Picks
                                                // *   *   *
                                                .button(2, 2, new Button.Builder()
                                                        .icon(() -> new ItemBuilder(Material.BEETROOT_SEEDS).name("&8&nPicks").lore("&7LMB: &c-\n&7RMB: &2+").toItem())
                                                        .lmb(interact -> {
                                                            // decrease
                                                            crate.picks = Util.clamp(crate.picks - 1, 1, crate.columns*9);
                                                            return EnumResult.REFRESH;
                                                        })
                                                        .rmb(interact -> {
                                                            // decrease
                                                            crate.picks = Util.clamp(crate.picks + 1, 1, crate.columns*9);
                                                            return EnumResult.REFRESH;
                                                        }))
                                                // *   *   *
                                                // Edit Pick Sound
                                                // *   *   *
                                                .childButton(4, 2, () -> new ItemBuilder(Material.JUKEBOX).name("&2Sound").lore("&8" + crate.sound).toItem(),
                                                        new TextMenu.TBuilder()
                                                                .title("Edit sound")
                                                                .onComplete((player, s) -> {
                                                                    try {
                                                                        crate.sound = Sound.valueOf(s.toUpperCase());
                                                                        return EnumResult.BACK;
                                                                    } catch (Exception e) {
                                                                        p.sendMessage("That sound does not exist");
                                                                    }
                                                                    return EnumResult.OK;
                                                                })
                                                )
                                        ).get()
                                );
                            }
                            return result;
                        })

                        .validate()
                /*
                 * View LootSets
                 */
                ).childButton(3, 1, () -> new ItemBuilder(Material.EXPERIENCE_BOTTLE).name("&6&lLoot").toItem(), new ParallaxMenu.PBuilder()
                        .title(ChatColor.GOLD + "Loot")
                        .parentButton(4, 5)
                        .action(self -> {
                            ArrayList<Button> result = new ArrayList<>();
                            for (var lootSet : Main.get().data.lootSets.values()) {
                                /*
                                 * List all LootSets
                                 */
                                result.add(new Button.Builder()
                                        .icon(() -> new ItemBuilder(lootSet.itemStack).lore("&8id: " + lootSet.id + "\n&8" + lootSet.loot.size() + " elements\n&8LMB: &2edit\n&8RMB: &cdelete").toItem())
                                        .child(self, new ParallaxMenu.PBuilder()
                                                .title("lootSet.id: " + lootSet.id)
                                                .parentButton(4, 5)
                                                .action(self1 -> {
                                                    ArrayList<Button> result1 = new ArrayList<>();
                                                    for (AbstractLoot a : lootSet.loot) {
                                                        ItemStack copy = a.getIcon();
                                                        Main.get().debug("a: " + a);
                                                        result1.add(new Button.Builder()
                                                                .icon(() -> new ItemBuilder(copy).lore(a + "\n&8LMB: &2edit\n&8RMB: &cdelete").toItem())
                                                                .child(self1, a.getMenuBuilder(), interact -> {
                                                                    if (lootSet.loot.size() > 1) {
                                                                        // delete
                                                                        lootSet.loot.remove(a);
                                                                        return EnumResult.REFRESH;
                                                                    }
                                                                    return EnumResult.OK;
                                                                })
                                                                .get());
                                                    }
                                                    return result1;
                                                })
                                                .childButton(2, 5, () -> new ItemBuilder(lootSet.itemStack.getType()).name("Edit...").toItem(), new SimpleMenu.SBuilder(5)
                                                        .title("Edit " + lootSet.id + " item")
                                                        .background()
                                                        .parentButton(4, 4)
                                                        .button(2, 1, inOutline)
                                                        .button(3, 2, inOutline)
                                                        .button(2, 3, inOutline)
                                                        .button(1, 2, inOutline)
                                                        // Edit ItemStack
                                                        .button(2, 2, new Button.Builder()
                                                                .icon(() -> lootSet.itemStack)
                                                                .lmb(interact -> {
                                                                    if (interact.heldItem != null)
                                                                        lootSet.itemStack = interact.heldItem;
                                                                    return EnumResult.GRAB_ITEM;
                                                                }))
                                                        // Edit Name
                                                        .childButton(6, 1, () -> new ItemBuilder(Material.NAME_TAG).name("&eName").lore("&8LMB: &aEdit").toItem(), new TextMenu.TBuilder()
                                                                .title("&8Edit name")
                                                                //.text(Util.toAlternateColorCodes('&', lootItem.itemStack.getItemMeta().getDisplayName()))
                                                                .leftInput(new Button.Builder().icon(() -> new ItemBuilder(Material.IRON_SWORD).mergeLexicals(lootSet.itemStack).toItem()))
                                                                .rightInput(new Button.Builder().icon(() -> new ItemBuilder(Material.IRON_SWORD).name("&8Use '&' for colors").toItem()))
                                                                .onClose(player -> EnumResult.BACK)
                                                                .onComplete((player, s) -> {
                                                                    if (s.isEmpty())
                                                                        return EnumResult.OK;
                                                                    lootSet.itemStack = new ItemBuilder(lootSet.itemStack).name(s).toItem();
                                                                    return EnumResult.BACK;
                                                                }))
                                                        // Edit Lore
                                                        .childButton(6, 3, () -> new ItemBuilder(Material.MAP).name("&7Lore").lore("&8LMB: &aEdit").toItem(), new TextMenu.TBuilder()
                                                                .title("&8Item lore")
                                                                .leftInput(new Button.Builder().icon(() -> new ItemBuilder(Material.IRON_SWORD).mergeLexicals(lootSet.itemStack).toItem()))
                                                                .rightInput(new Button.Builder().icon(() -> new ItemBuilder(Material.IRON_SWORD).name("&8Use '&7&&8' for colors").lore("&8Use '&7\\n&8' for multiline").toItem()))
                                                                //.leftInput(new Button.Builder().icon(() -> itemStack))
                                                                //.rightInput(new Button.Builder().icon(() -> new ItemBuilder(itemStack.getType()).name("&8Use '&' for colors").lore("&7Ignore the &cX &7>").toItem()))
                                                                .onClose(player -> EnumResult.BACK)
                                                                .onComplete((player, s) -> {
                                                                    if (s.isEmpty())
                                                                        return EnumResult.OK;
                                                                    lootSet.itemStack = new ItemBuilder(lootSet.itemStack).lore(s.replace("\\n", "\n")).toItem();
                                                                    return EnumResult.BACK;
                                                                }))
                                                        // Edit CustomModelData
                                                        .childButton(6, 2, () -> new ItemBuilder(Material.PLAYER_HEAD).skull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzQ2NDg3NGRmNDUyYzFkNzE3ZWRkZDBmYjNiODQ4MjAyYWQxNTU3MTI0NWFmNmZhZGUyZWNmNTE0ZjNjODBiYiJ9fX0=").name("&7CustomModelData").lore("&8LMB: &aEdit").toItem(), new TextMenu.TBuilder()
                                                                .title("&8CustomModelData")
                                                                .leftInput(new Button.Builder().icon(() -> {
                                                                    ItemMeta meta = lootSet.itemStack.getItemMeta();
                                                                    ItemBuilder builder = new ItemBuilder(Material.IRON_SWORD);
                                                                    if (meta != null && meta.hasCustomModelData())
                                                                        builder.name("CustomModelData: " + meta.getCustomModelData());
                                                                    else builder.name("CustomModelData: none");
                                                                    return builder.toItem();
                                                                }))
                                                                .rightInput(new Button.Builder().icon(() -> new ItemBuilder(Material.IRON_SWORD).name("&8Input an integer").toItem()))
                                                                //.leftInput(new Button.Builder().icon(() -> itemStack))
                                                                //.rightInput(new Button.Builder().icon(() -> new ItemBuilder(itemStack.getType()).name("&8Use '&' for colors").lore("&7Ignore the &cX &7>").toItem()))
                                                                .onClose(player -> EnumResult.BACK)
                                                                .onComplete((player, s) -> {
                                                                    if (s.isEmpty())
                                                                        return EnumResult.OK;

                                                                    int i;
                                                                    try {
                                                                        i = Integer.parseInt(s);
                                                                    } catch (Exception e00) {
                                                                        player.sendMessage("&cInput an integer");
                                                                        return EnumResult.OK;
                                                                    }

                                                                    lootSet.itemStack = new ItemBuilder(lootSet.itemStack).customModelData(i).toItem();
                                                                    return EnumResult.BACK;
                                                                }))
                                                )



                                                .childButton(6, 5, () -> new ItemBuilder(Material.NETHER_STAR).name("&2New...").toItem(), new ParallaxMenu.PBuilder()
                                                        .title("New AbstractLoot")
                                                        //.onClose(player -> EnumResult.BACK)
                                                        .parentButton(4, 5)
                                                        .action(self1 -> {
                                                            ArrayList<Button> result1 = new ArrayList<>();
                                                            for (var menuClazz : LootCratesAPI.lootClasses) {
                                                                //AbstractLoot aLootInstance = new a
                                                                result1.add(new Button.Builder()
                                                                        .icon(() -> new ItemBuilder(Material.GOLD_INGOT).name(menuClazz.getSimpleName()).toItem())
                                                                        .lmb(interact -> {
                                                                            AbstractMenu.Builder menu = lootSet.addLoot(
                                                                                    (AbstractLoot) ReflectionUtil.invokeConstructor(menuClazz)).getMenuBuilder();
                                                                            menu.parent(self1.parentMenuBuilder);
                                                                            return EnumResult.OPEN(menu);
                                                                        })
                                                                        .get());
                                                            }
                                                            return result1;
                                                        })
                                                ),
                                                // RMB event
                                                interact -> {
                                                    if (Main.get().data.lootSets.size() > 1) {
                                                        Main.get().data.lootSets.remove(lootSet.id);
                                                        for (Crate crate : Main.get().data.crates.values()) {
                                                            Integer removed = crate.lootByWeight.remove(lootSet);
                                                            if (removed != null)
                                                                crate.weightsToSums();
                                                        }
                                                        return EnumResult.REFRESH;
                                                    }
                                                    return EnumResult.OK;
                                                }
                                        ).get()
                                );
                            }
                            return result;
                        })
                        .childButton(6, 5, () -> new ItemBuilder(Material.NETHER_STAR).name("&2New...").toItem(), new TextMenu.TBuilder()
                                .title("New LootSet")
                                .onClose(player -> EnumResult.BACK)
                                .onComplete((player, text) -> {
                                    if (!text.isEmpty() && !Main.get().data.lootSets.containsKey(text)) {
                                        // add that lootgroup
                                        Main.get().data.lootSets.put(text,
                                                new LootSet(text, new ItemStack(Material.RED_STAINED_GLASS),
                                                        new ArrayList<>(List.of(new LootItem()))));

                                    }
                                    return EnumResult.BACK;
                                })
                        )
                        .validate()
                )
                /*
                 * Global Fireworks Edit
                 */
                .childButton(5, 1, () -> new ItemBuilder(Material.FIREWORK_ROCKET).name("&e&lFireworks").toItem(), new SimpleMenu.SBuilder(5)
                        .title("&8Firework")
                        .background()
                        .button(4, 0, inOutline)
                        .button(3, 1, inOutline)
                        .button(5, 1, inOutline)
                        .button(4, 2, inOutline)
                        .button(1, 1, new Button.Builder()
                                .icon(() -> new ItemBuilder(Material.FIREWORK_STAR).fireworkEffect(Main.get().data.fireworkEffect).toItem()))
                        .button(4, 1, new Button.Builder()
                                .icon(() -> new ItemBuilder(Material.FIREWORK_STAR).fireworkEffect(Main.get().data.fireworkEffect).toItem())
                                .lmb(interact -> {
                                    if (interact.heldItem != null) {
                                        if (interact.heldItem.getItemMeta() instanceof FireworkEffectMeta meta && meta.hasEffect()) {
                                            Main.get().data.fireworkEffect = meta.getEffect();
                                            return EnumResult.REFRESH;
                                        }
                                        else interact.player.sendMessage(ChatColor.YELLOW + "must have effect");
                                    }

                                    return EnumResult.GRAB_ITEM;
                                }))
                        .parentButton(4, 4)
                )
                /*
                 * Misc settings
                 */
                .childButton(7, 1, () -> new ItemBuilder(Material.NETHERITE_SCRAP).name("&8&lMisc").toItem(), new SimpleMenu.SBuilder(5)
                        .title("&8Misc")
                        .button(1, 1, new Button.Builder()
                                .icon(() -> new ItemBuilder(Material.COMMAND_BLOCK).name("&e&lToggle Debug").lore(Main.get().data.debug ? "&2enabled" : "&cdisabled").toItem())
                                .lmb(interact -> {Main.get().data.debug ^= true; return EnumResult.REFRESH;}))
                        .parentButton(4, 4)
                )
                /*
                 *
                 */
                .validate().open(p);


    }

}
