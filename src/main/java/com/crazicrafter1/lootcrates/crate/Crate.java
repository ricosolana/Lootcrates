package com.crazicrafter1.lootcrates.crate;

import com.crazicrafter1.crutils.ColorMode;
import com.crazicrafter1.crutils.ColorUtil;
import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.crutils.Util;
import com.crazicrafter1.gapi.*;
import com.crazicrafter1.lootcrates.*;
import com.crazicrafter1.lootcrates.Main;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static com.crazicrafter1.lootcrates.Editor.ColorDem;
import static com.crazicrafter1.lootcrates.Lang.L;

public class Crate implements ConfigurationSerializable {

    /// TODO Use this instead
    /// https://stackoverflow.com/questions/1761626/weighted-random-numbers

    // can be used to sort any given lootgroup map
    private static <T> LinkedHashMap<T, Integer> sortByValue(HashMap<T, Integer> hm) {
        // Create a list from elements of HashMap
        List<Map.Entry<T, Integer> > list =
                new LinkedList<>(hm.entrySet());

        // Sort the list
        list.sort(Map.Entry.comparingByValue());

        // put data from sorted list to hashmap
        LinkedHashMap<T, Integer> temp = new LinkedHashMap<>();
        for (Map.Entry<T, Integer> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }

    /**
     * [[[weights -> sums]]]
     */
    public void weightsToSums() {
        lootBySum = sortByValue(lootByWeight);

        int last = 0;
        for (Map.Entry<LootSet, Integer> entry : lootBySum.entrySet()) {
            entry.setValue(last + entry.getValue());
            last = entry.getValue();
        }

        this.totalWeights = last;
    }

    /**
     * [[[sums -> weights]]]
     */
    public void sumsToWeights() {
        // reset
        lootByWeight = new LinkedHashMap<>();

        int prevSum = 0;
        for (Map.Entry<LootSet, Integer> entry : lootBySum.entrySet()) {
            int weight = entry.getValue() - prevSum;

            lootByWeight.put(entry.getKey(), weight);

            prevSum = entry.getValue();
        }

        this.totalWeights = prevSum;
    }

    public static class Language {
        public String itemStackDisplayName;
        public String itemStackLore;

        public String title;
    }

    // Any translation applicable values below
    // such as title, and itemStack name
    // are defaulted to English (en_us).

    // so a check will involve checking whether
    // the player language is en_us, and if not,
    // then attempt a translation

    public String id;
    public ItemBuilder item;
    public String title;
    public int columns;
    public int picks;
    public Sound sound;

    public LinkedHashMap<LootSet, Integer> lootBySum;
    public HashMap<LootSet, Integer> lootByWeight;
    public int totalWeights;

    public Crate(String id, ItemStack itemStack, String title, int columns, int picks, Sound sound) {
        this.id = id;
        this.item = ItemBuilder.copyOf(LootCratesAPI.makeCrate(itemStack, id));
        this.title = ColorUtil.render(title);
        this.columns = columns;
        this.picks = picks;
        this.sound = sound;
    }

    public Crate(Map<String, Object> args) {
        title = ColorUtil.render((String) args.get("title"));
        columns = (int) args.get("columns");
        picks = (int) args.get("picks");
        sound = Sound.valueOf((String) args.get("sound"));
        lootBySum = sortByValue((LinkedHashMap<LootSet, Integer>) args.get("weights"));

        int rev = Main.get().rev;
        if (rev < 2)
            item = ItemBuilder.mutable((ItemStack) args.get("itemStack"));
        else if (rev == 2)
            item = ((ItemBuilder) args.get("item"));
    }

    /**
     * Assumes that the map is cumulative-weight sorted in config
     * unknown whether config map retains original order
     */
    LootSet getRandomLootSet() {
        int rand = Util.randomRange(0, totalWeights-1);

        /// could use binary search, which is faster for larger sets,
        /// where the random weight is closer to the upper bound
        /// current search: O(random weight - max weight)
        /// binary search: O(random weight
        for (Map.Entry<LootSet, Integer> entry : this.lootBySum.entrySet()) {
            if (entry.getValue() > rand) return entry.getKey();
        }

        return null;
    }

    public String getFormattedPercent(LootSet lootGroup) {
        return String.format("%.02f%%", 100.f * ((float) lootByWeight.get(lootGroup)/(float)totalWeights));
    }

    public String getFormattedFraction(LootSet lootGroup) {
        return String.format("%d/%d", lootByWeight.get(lootGroup), totalWeights);
    }

    /**
     * Return the macro formatted item, or unformatted if player is null
     * @param p player
     * @return the formatted item
     */
    public ItemStack itemStack(@Nullable Player p, boolean renderAll) {
        ItemBuilder item = ItemBuilder.copyOf(this.item);

        Lang.Unit unit = Main.get().lang.getUnit(p);

        item.macro("%", "lc_picks", "" + picks)
                .macro("%", "lc_id", "" + id)
                .macro("%", "lc_lscount", "" + lootBySum.size())
                .placeholders(p);

        if (unit != null) {
            Language lang = unit.crates.get(id);
            item.name(lang.itemStackDisplayName)
                    .lore(lang.itemStackLore);
        }

        return item
                .renderAll(renderAll)
                .build();
    }

    public String title(@Nonnull Player p) {
        Lang.Unit unit = Main.get().lang.getUnit(p);

        if (unit == null) {
            return Util.placeholders(p, title);
        }

        Language lang = unit.crates.get(id);

        return Util.placeholders(p, lang.title);
    }

    @Override
    public String toString() {
        return "itemStack: " + item + "\n" +
                "title: " + title + "\n" +
                "size: " + title + "\n" +
                "picks: " + picks + "\n" +
                "sound: " + sound + "\n" +
                "weights: " + lootBySum + "\n";
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<>();

        result.put("item", item);
        result.put("title", title);
        result.put("columns", columns);
        result.put("picks", picks);
        result.put("sound", sound.name());
        result.put("weights", lootBySum);

        return result;
    }

    public AbstractMenu.Builder getBuilder() {
        return new SimpleMenu.SBuilder(5)
                .title(p -> id)
                .background()
                .parentButton(4, 4)
                // *   *   *
                // Edit Crate ItemStack
                // *   *   *
                .childButton(1, 1, p -> ItemBuilder.copyOf(item.getMaterial()).name("&8&n" + L(p, Lang.A.ItemStack)).lore("&7" + L(p, Lang.A.LMB) + ": &a" + L(p, Lang.A.Edit)).build(), new ItemModifyMenu()
                        .build(item.build(), (itemStack -> {
                            // This will break NBT if the server is not reloaded
                            //this.itemStack = itemStack;

                            // Several options here
                            // Should the name and lore be merged along also?
                            // or just the material?
                            return ItemBuilder.mutable(this.item).combine(itemStack).material(itemStack.getType()).build();
                            //this.itemStack.setType(itemStack.getType());
                        }))
                )
                // Edit Inventory Title
                .childButton(3, 1, p -> ItemBuilder.copyOf(Material.PAPER).name("&e&n" + L(p, Lang.A.Title) + "&r&e: " + title).lore("&7" + L(p, Lang.A.LMB) + ": &a" + L(p, Lang.A.Edit)).build(), new TextMenu.TBuilder()
                        .title(p -> L(p, Lang.A.Title))
                        .leftRaw(p -> ColorMode.INVERT.a(title))
                        .onClose((player) -> Result.PARENT())
                        .right(p -> ColorDem)
                        .onComplete((p, s, b) -> {
                            if (!s.isEmpty()) {
                                title = ColorMode.RENDER.a(s);
                                return Result.PARENT();
                            }

                            return Result.TEXT(L(p, Lang.A.Invalid));
                        })
                )
                // *   *   *
                // Edit LootSets
                // *   *   *
                .childButton(5, 1, p -> ItemBuilder.fromModernMaterial("EXPERIENCE_BOTTLE").name("&6&n" + L(p, Lang.A.Loot)).lore("&7" + L(p, Lang.A.LMB) + ": &a" + L(p, Lang.A.Edit)).build(), new ParallaxMenu.PBuilder()
                        .title(p -> L(p, Lang.A.Loot))
                        .parentButton(4, 5)
                        .onClose((player) -> Result.PARENT())
                        .addAll((builder, p) -> {
                            ArrayList<Button> result1 = new ArrayList<>();

                            for (LootSet lootSet : Main.get().data.lootSets.values()) {
                                Integer weight = lootByWeight.get(lootSet);
                                Button.Builder btn = new Button.Builder();
                                ItemBuilder b = ItemBuilder.copyOf(lootSet.item.getMaterial()).name("&8" + lootSet.id);
                                if (weight != null) {
                                    b.lore("&7" + getFormattedFraction(lootSet) + "\n" +
                                            "&7" + getFormattedPercent(lootSet) + "\n" +
                                            L(p, Lang.A.LMB) + " &c-\n" +
                                            L(p, Lang.A.RMB) + " &a+\n" +
                                            "&f" + L(Lang.A.MMB) + ": &7" + L(Lang.A.Toggle) + "\n" +
                                            "&7" + L(Lang.A.SHIFT_Mul) + "&r&7: x5").glow(true);
                                    btn.mmb(interact -> {
                                        // toggle inclusion
                                        lootByWeight.remove(lootSet);
                                        weightsToSums();
                                        return Result.REFRESH();
                                    }).lmb(interact -> {
                                        // decrement
                                        int change = interact.shift ? 5 : 1;

                                        lootByWeight.put(lootSet, Util.clamp(weight - change, 1, Integer.MAX_VALUE));
                                        weightsToSums();

                                        return Result.REFRESH();
                                    }).rmb(interact -> {
                                        // decrement
                                        int change = interact.shift ? 5 : 1;

                                        lootByWeight.put(lootSet, Util.clamp(weight + change, 1, Integer.MAX_VALUE));
                                        weightsToSums();

                                        return Result.REFRESH();
                                    });
                                } else {
                                    b.lore("&f" + L(Lang.A.MMB) + ": &7" + L(Lang.A.Toggle));
                                    btn.mmb(interact -> {
                                        lootByWeight.put(lootSet, 1);
                                        weightsToSums();
                                        return Result.REFRESH();
                                    });
                                }
                                result1.add(btn.icon(p1 -> b.build()).get());
                            }

                            return result1;
                        })
                )
                // *   *   *
                // Edit Columns
                // *   *   *
                .button(7, 1, new Button.Builder()
                        .icon(p -> ItemBuilder.copyOf(Material.LADDER).name("&8&n" + L(Lang.A.Columns) + "&r&8: &7" + columns).lore(L(Lang.A.LMB) + " &c-\n" +  L(Lang.A.RMB) + " &a+").amount(columns).build())
                        .lmb(interact -> {
                            // decrease
                            columns = Util.clamp(columns - 1, 1, 6);
                            return Result.REFRESH();
                        })
                        .rmb(interact -> {
                            // decrease
                            columns = Util.clamp(columns + 1, 1, 6);
                            return Result.REFRESH();
                        }))
                // *   *   *
                // Edit Picks
                // *   *   *
                .button(2, 3, new Button.Builder()
                        .icon(p -> ItemBuilder.copyOf(Material.MELON_SEEDS).name("&8&n" + L(Lang.A.Picks) + "&r&8: &7" + picks).lore(L(Lang.A.LMB) + " &c-\n" +  L(Lang.A.RMB) + " &a+").amount(picks).build())
                        .lmb(interact -> {
                            // decrease
                            picks = Util.clamp(picks - 1, 1, columns*9);
                            return Result.REFRESH();
                        })
                        .rmb(interact -> {
                            // decrease
                            picks = Util.clamp(picks + 1, 1, columns*9);
                            return Result.REFRESH();
                        }))
                // *   *   *
                // Edit Pick Sound
                // *   *   *
                .childButton(6, 3, p -> ItemBuilder.copyOf(Material.JUKEBOX).name("&a&n" + L(Lang.A.Sound) + "&r&a: &r&7" + sound.name()).lore("&7" + L(Lang.A.LMB) + ": &a" + L(Lang.A.Edit)).build(),
                        new TextMenu.TBuilder()
                                .title(p -> L(Lang.A.Sound))
                                .leftRaw(p -> Lang.A.Lorem_ipsum)
                                .right(p -> L(Lang.A.Input_a_sound))
                                .onClose((player) -> Result.PARENT())
                                .onComplete((p, s, b) -> {
                                    try {
                                        Sound sound = Sound.valueOf(s.toUpperCase());
                                        this.sound = sound;
                                        p.playSound(p.getLocation(), sound, 1, 1);
                                        return Result.PARENT();
                                    } catch (Exception e) {
                                        return Result.TEXT(L(Lang.A.Invalid));
                                    }
                                })
                );
    }

}
