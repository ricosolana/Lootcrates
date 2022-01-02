package com.crazicrafter1.lootcrates;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.crutils.ReflectionUtil;
import com.crazicrafter1.crutils.Util;
import com.crazicrafter1.gapi.*;
import com.crazicrafter1.lootcrates.crate.Crate;
import com.crazicrafter1.lootcrates.crate.LootSet;
import com.crazicrafter1.lootcrates.crate.loot.ILoot;
import com.crazicrafter1.lootcrates.crate.loot.LootItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkEffectMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class Editor {

    //public static final Button.Builder inOutline = new Button.Builder().icon(() -> new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name("&7Set to").toItem());

    public static final Button.Builder IN_OUTLINE = new Button.Builder().icon(() -> new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name("&7Set to").toItem());

    public static final String LORE_LMB_EDIT = "&7LMB: &aEdit";
    public static final String LORE_RMB_DEL = "&7RMB: &cDelete";
    public static final String LORE_LMB_NUM = "&7LMB&r&7: &c-";
    public static final String LORE_RMB_NUM = "&7RMB&r&7: &a+";
    public static final String LORE_SHIFT_NUM = "&7SHIFT&r&7: x5";

    public static final ItemStack ITEM_NEW = new ItemBuilder(Material.NETHER_STAR).name("&6New").toItem();
    public static final String NAME_EDIT = "&aEdit";

    @SuppressWarnings("SpellCheckingInspection")
    public static final String BASE64_CUSTOM_MODEL_DATA = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzQ2NDg3NGRmNDUyYzFkNzE3ZWRkZDBmYjNiODQ4MjAyYWQxNTU3MTI0NWFmNmZhZGUyZWNmNTE0ZjNjODBiYiJ9fX0=";

    public static String COLOR_PREFIX = "&7: &bcolors" + (ReflectionUtil.isAtLeastVersion("1_16") ?
            "&7'#&': &#2367fbc&#3f83fbo&#5a9ffcl&#76bbfco&#91d7fcr&#acf2fds" : "&7'&': &fcolors");
    private static final String LOREM_IPSUM = "Lorem ipsum";

    @SuppressWarnings("DanglingJavadoc")
    public static void open(Player p) {

        new SimpleMenu.SBuilder(3)
                .title("editor", true)
                .background()
                /*********************\
                 *                   *
                 * Global Crate List *
                 *                   *
                 \*******************/
                .childButton(1, 1, () -> new ItemBuilder(Material.CHEST).name("&3&lCrates").toItem(), new ParallaxMenu.PBuilder()
                        .title("crates", true)
                        .parentButton(4, 5)
                        // *       *      *
                        // Add Crate button
                        // *       *      *
                        .childButton(5, 5, () -> ITEM_NEW, new TextMenu.TBuilder()
                                .title("new crate", true)
                                .left(() -> LOREM_IPSUM)
                                .onClose((player) -> EnumResult.BACK)
                                .onComplete((player, s) -> {
                                    if (s.isEmpty() || Main.get().data.crates.containsKey(s)) {
                                        return EnumResult.TEXT("Input a unique key");
                                    }
                                    Crate crate = new Crate(s, new ItemBuilder(Material.ENDER_CHEST).name("my new crate").toItem(), "select loot", 3, 4, Sound.ENTITY_EXPERIENCE_ORB_PICKUP);

                                    crate.lootBySum = new LinkedHashMap<>();
                                    crate.lootBySum.put(Main.get().data.lootSets.values().iterator().next(), 1);
                                    crate.sumsToWeights();

                                    Main.get().data.crates.put(s, crate);

                                    return EnumResult.BACK;
                                })
                        )
                        .action(self -> {
                            ArrayList<Button> result = new ArrayList<>();
                            for (Map.Entry<String, Crate> entry : Main.get().data.crates.entrySet()) {
                                Crate crate = entry.getValue();
                                result.add(new Button.Builder()
                                        //                                                  .lore("&8id: " + lootSet.id + "\n" + "&8" + lootSet.loot.size() + " elements" + "\n" + LORE_LMB_EDIT + "\n" + LORE_RMB_DEL)
                                        .icon(() -> new ItemBuilder(crate.itemStack).lore("&8id: " + crate.id + "\n" + LORE_LMB_EDIT + "\n" + LORE_RMB_DEL).toItem())
                                        .child(self, new SimpleMenu.SBuilder(5)
                                                .title(crate.id, true)
                                                .background()
                                                .parentButton(4, 4)
                                                // *   *   *
                                                // Edit Crate ItemStack
                                                // *   *   *
                                                .childButton(1, 1, () -> new ItemBuilder(crate.itemStack.getType()).name("&8&nItemStack").lore(LORE_LMB_EDIT).toItem(), new ItemMutateMenuBuilder()
                                                        .build(crate.itemStack, (itemStack -> {
                                                            //Main.get().info("consumer: " + itemStack);
                                                            crate.itemStack = itemStack;
                                                        }))
                                                )
                                                // *   *   *
                                                // Edit Inventory Title
                                                // *   *   *
                                                .childButton(3, 1, () -> new ItemBuilder(Material.PAPER).name("&e&nTitle&r&e: " + crate.title).lore(LORE_LMB_EDIT).toItem(), new TextMenu.TBuilder()
                                                        .title("title", true)
                                                        .left(() -> Util.toAlternateColorCodes('&', crate.title))
                                                        .onClose(player -> EnumResult.BACK)
                                                        //.leftInput(SAMPLE_LEFT)
                                                        .right(() -> COLOR_PREFIX)
                                                        .onComplete((player, s) -> {
                                                            // set name if it is not empty
                                                            if (!s.isEmpty()) {
                                                                crate.title = Util.format("" + s);
                                                                return EnumResult.BACK;
                                                            }

                                                            return EnumResult.TEXT("Invalid");
                                                        })
                                                        .onClose(player -> EnumResult.BACK)
                                                )
                                                // *   *   *
                                                // Edit LootSets
                                                // *   *   *
                                                .childButton(5, 1, () -> new ItemBuilder(Material.EXPERIENCE_BOTTLE).name("&6&nLoot").lore(LORE_LMB_EDIT).toItem(), new ParallaxMenu.PBuilder()
                                                        .title("lootSets", true)
                                                        .parentButton(4, 5)
                                                        .action(builder -> {
                                                            ArrayList<Button> result1 = new ArrayList<>();

                                                            for (LootSet lootSet : Main.get().data.lootSets.values()) {
                                                                Integer weight = crate.lootByWeight.get(lootSet);
                                                                Button.Builder btn = new Button.Builder();
                                                                ItemBuilder b = new ItemBuilder(lootSet.itemStack.getType()).name("&8" + lootSet.id);
                                                                if (weight != null) {
                                                                    b.lore("&7" + crate.getFormattedFraction(lootSet) + "\n" +
                                                                            "&7" + crate.getFormattedPercent(lootSet) + "\n" +
                                                                            LORE_LMB_NUM + "\n" +
                                                                            LORE_RMB_NUM + "\n" +
                                                                            "&fMMB: &7toggle" + "\n" +
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
                                                        .icon(() -> new ItemBuilder(Material.LADDER).name("&8&nColumns&r&8: &7" + crate.columns).lore(LORE_LMB_NUM + "\n" + LORE_RMB_NUM).count(crate.columns).toItem())
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
                                                .button(2, 3, new Button.Builder()
                                                        .icon(() -> new ItemBuilder(Material.MELON_SEEDS).name("&8&nPicks&r&8: &7" + crate.picks).lore(LORE_LMB_NUM + "\n" + LORE_RMB_NUM).count(crate.picks).toItem())
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
                                                .childButton(6, 3, () -> new ItemBuilder(Material.JUKEBOX).name("&a&nSound&r&a: &r&7" + crate.sound).lore(LORE_LMB_EDIT).toItem(),
                                                        new TextMenu.TBuilder()
                                                                .title("sound", true)
                                                                .left(() -> LOREM_IPSUM)
                                                                .right(() -> "Input a sound")
                                                                .onClose(player -> EnumResult.BACK)
                                                                .onComplete((player, s) -> {
                                                                    try {
                                                                        Sound sound = Sound.valueOf(s.toUpperCase());
                                                                        crate.sound = sound;
                                                                        p.playSound(p.getLocation(), sound, 1, 1);
                                                                        return EnumResult.BACK;
                                                                    } catch (Exception e) {
                                                                        return EnumResult.TEXT("Invalid sound");
                                                                    }
                                                                })
                                                ),
                                                /// RMB - delete crate
                                                interact -> {
                                                    Main.get().data.crates.remove(crate.id);
                                                    return EnumResult.REFRESH;
                                                }
                                        ).get()
                                );
                            }
                            return result;
                        })
                /*
                 * View LootSets
                 */
                ).childButton(3, 1, () -> new ItemBuilder(Material.EXPERIENCE_BOTTLE).name("&6&lLoot").toItem(), new ParallaxMenu.PBuilder()
                        .title("lootSets", true)
                        .parentButton(4, 5)
                        .action(self -> {
                            ArrayList<Button> result = new ArrayList<>();
                            for (LootSet lootSet : Main.get().data.lootSets.values()) {
                                /*
                                 * List all LootSets
                                 */
                                result.add(new Button.Builder()
                                        .icon(() -> new ItemBuilder(lootSet.itemStack).lore("&8id: " + lootSet.id + "\n" + "&8" + lootSet.loot.size() + " elements" + "\n" + LORE_LMB_EDIT + "\n" + LORE_RMB_DEL).toItem())
                                        .child(self, new ParallaxMenu.PBuilder()
                                                .title(lootSet.id, true)
                                                .parentButton(4, 5)
                                                .action(self1 -> {
                                                    ArrayList<Button> result1 = new ArrayList<>();
                                                    for (ILoot a : lootSet.loot) {
                                                        ItemStack copy = a.getIcon(null);
                                                        result1.add(new Button.Builder()
                                                                .icon(() -> new ItemBuilder(copy).lore(a + "\n" + LORE_LMB_EDIT + "\n" + LORE_RMB_DEL).toItem())
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
                                                .childButton(3, 5, () -> new ItemBuilder(lootSet.itemStack/*.getType()*/).name(NAME_EDIT).toItem(), new ItemMutateMenuBuilder()
                                                        .build(lootSet.itemStack, itemStack -> lootSet.itemStack = itemStack))
                                                .childButton(5, 5, () -> ITEM_NEW, new ParallaxMenu.PBuilder()
                                                        .title("new loot", true)
                                                        //.onClose(player -> EnumResult.BACK)
                                                        .parentButton(4, 5)
                                                        .action(self1 -> {
                                                            ArrayList<Button> result1 = new ArrayList<>();
                                                            for (Class<? extends ILoot> menuClazz : LootCratesAPI.lootClasses) {
                                                                //AbstractLoot aLootInstance = new a
                                                                result1.add(new Button.Builder()
                                                                        // This causes a nullptr because it is instantly constructed?
                                                                        //.icon(() -> new ItemBuilder(Material.GOLD_INGOT).name(menuClazz.getSimpleName()).toItem())
                                                                        //.child(self1.parentMenuBuilder, lootSet.addLoot(
                                                                        //        (ILoot) ReflectionUtil.invokeConstructor(menuClazz)).getMenuBuilder())

                                                                        .icon(() -> new ItemBuilder(Material.GOLD_INGOT).name(menuClazz.getSimpleName()).toItem())
                                                                        .lmb(interact -> {
                                                                            AbstractMenu.Builder menu = lootSet.addLoot(
                                                                                    (ILoot) ReflectionUtil.invokeConstructor(menuClazz)).getMenuBuilder();
                                                                            menu.parent(self1.parentMenuBuilder);
                                                                            return EnumResult.OPEN(menu);
                                                                        })
                                                                        .get());
                                                            }
                                                            return result1;
                                                        })
                                                ),
                                                // RMB - delete lootSet
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
                        .childButton(5, 5, () -> ITEM_NEW, new TextMenu.TBuilder()
                                .title("new lootSet", true)
                                .left(() -> LOREM_IPSUM)
                                .onClose(player -> EnumResult.BACK)
                                .onComplete((player, text) -> {
                                    if (!text.isEmpty() && !Main.get().data.lootSets.containsKey(text)) {
                                        // Java 16
                                        //Main.get().data.lootSets.put(text,
                                        //        new LootSet(text, new ItemStack(Material.RED_STAINED_GLASS),
                                        //                new ArrayList<>(List.of(new LootItem()))));
                                        Main.get().data.lootSets.put(text,
                                                new LootSet(text, new ItemStack(Material.GLOWSTONE_DUST),
                                                        new ArrayList<>(Collections.singletonList(new LootItem()))));

                                    }
                                    return EnumResult.BACK;
                                })
                        )
                )
                /*
                 * Global Fireworks Edit
                 */
                .childButton(5, 1, () -> new ItemBuilder(Material.FIREWORK_ROCKET).name("&e&lFireworks").toItem(), new SimpleMenu.SBuilder(5)
                        .title("firework", true)
                        .background()
                        .button(4, 0, IN_OUTLINE)
                        .button(3, 1, IN_OUTLINE)
                        .button(5, 1, IN_OUTLINE)
                        .button(4, 2, IN_OUTLINE)
                        .button(1, 1, new Button.Builder()
                                .icon(() -> new ItemBuilder(Material.FIREWORK_STAR).fireworkEffect(Main.get().data.fireworkEffect).toItem()))
                        .button(4, 1, new Button.Builder()
                                .icon(() -> new ItemBuilder(Material.FIREWORK_STAR).fireworkEffect(Main.get().data.fireworkEffect).toItem())
                                .lmb(interact -> {
                                    if (interact.heldItem != null) {
                                        //if (interact.heldItem.getItemMeta() instanceof FireworkEffectMeta meta && meta.hasEffect()) {
                                        if (interact.heldItem.getItemMeta() instanceof FireworkEffectMeta) {
                                            FireworkEffectMeta meta = (FireworkEffectMeta) interact.heldItem.getItemMeta();
                                            if (meta.hasEffect()) {
                                                Main.get().data.fireworkEffect = meta.getEffect();
                                                return EnumResult.REFRESH;
                                            }
                                        }
                                        interact.player.sendMessage(ChatColor.YELLOW + "must have effect");
                                    }

                                    return EnumResult.GRAB_ITEM;
                                }))
                        .parentButton(4, 4)
                )
                /*
                 * Misc settings
                 */
                .childButton(7, 1, () -> new ItemBuilder(Material.IRON_HORSE_ARMOR).name("&8&lMisc").toItem(), new SimpleMenu.SBuilder(5)
                        .title("misc", true)
                        .button(1, 1, new Button.Builder()
                                .icon(() -> new ItemBuilder(Material.COMMAND_BLOCK).name("&e&lToggle Debug").lore(Main.get().data.debug ? "&2enabled" : "&cdisabled").toItem())
                                .lmb(interact -> {Main.get().data.debug ^= true; return EnumResult.REFRESH;}))
                        .parentButton(4, 4)
                )
                /*
                 * Finally open
                 */
                .open(p);


    }

}
