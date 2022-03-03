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

        item.macro("%", "lc_picks", "" + picks)
                .macro("%", "lc_id", "" + id)
                .macro("%", "lc_lscount", "" + lootBySum.size())
                .placeholders(p);

        return item
                .renderAll(renderAll)
                .build();
    }

    public String title(@Nonnull Player p) {
        return Util.placeholders(p, title);
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
                .childButton(1, 1, p -> ItemBuilder.copyOf(item).name(Lang.EDIT_ITEM).lore(Lang.LMB_EDIT).build(), new ItemModifyMenu()
                        .build(item.build(), (itemStack -> {
                            // Several options here
                            // Should the name and lore be merged along also?
                            // or just the material?
                            this.item.material(itemStack.getType());

                            String base64 = ItemBuilder.mutable(itemStack).getSkull();
                            if (base64 != null)
                                this.item.skull(base64);
                            return this.item.combine(itemStack).build();
                            //this.itemStack.setType(itemStack.getType());
                        }))
                )
                // Edit Inventory Title
                .childButton(3, 1, p -> ItemBuilder.copyOf(Material.PAPER).name(String.format(Lang.EDIT_TITLE, title)).lore(Lang.LMB_EDIT).build(), new TextMenu.TBuilder()
                        .title(p -> Lang.TITLE)
                        .leftRaw(p -> ColorMode.INVERT_RENDERED.a(title))
                        .onClose((player) -> Result.PARENT())
                        .right(p -> "Special formatting", ColorMode.AS_IS, p -> Editor.COLORS, ColorMode.AS_IS)
                        .onComplete((p, s, b) -> {
                            if (!s.isEmpty()) {
                                title = ColorMode.RENDER_MARKERS.a(s);
                                return Result.PARENT();
                            }

                            return Result.TEXT(Lang.ERR_INVALID);
                        })
                )
                // *   *   *
                // Edit LootSets
                // *   *   *
                .childButton(5, 1, p -> ItemBuilder.fromModernMaterial("EXPERIENCE_BOTTLE").name(Lang.LOOT).lore(Lang.LMB_EDIT).build(), new ParallaxMenu.PBuilder()
                        .title(p -> Lang.LOOT)
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
                                            Lang.LMB_DEC + "\n" +
                                            Lang.RMB_INC + "\n" +
                                            Lang.MMB_TOGGLE + "\n" +
                                            Lang.SHIFT_MUL).glow(true);
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
                                    b.lore(Lang.MMB_TOGGLE);
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
                        .icon(p -> ItemBuilder.copyOf(Material.LADDER).name(String.format(Lang.BUTTON_COLUMNS, columns)).lore(Lang.LMB_DEC + "\n" + Lang.RMB_INC).amount(columns).build())
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
                        .icon(p -> ItemBuilder.copyOf(Material.MELON_SEEDS).name(String.format(Lang.BUTTON_PICKS, picks)).lore(Lang.LMB_DEC + "\n" + Lang.RMB_INC).amount(picks).build())
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
                .childButton(6, 3, p -> ItemBuilder.copyOf(Material.JUKEBOX).name(String.format(Lang.BUTTON_SOUND, sound)).lore(Lang.LMB_EDIT).build(),
                        new TextMenu.TBuilder()
                                .title(p -> Lang.TITLE_SOUND)
                                .leftRaw(p -> "Lorem ipsum")
                                .right(p -> Lang.INPUT_SOUND)
                                .onClose((player) -> Result.PARENT())
                                .onComplete((p, s, b) -> {
                                    try {
                                        Sound sound = Sound.valueOf(s.toUpperCase());
                                        this.sound = sound;
                                        p.playSound(p.getLocation(), sound, 1, 1);
                                        return Result.PARENT();
                                    } catch (Exception e) {
                                        return Result.TEXT(Lang.ERR_INVALID);
                                    }
                                })
                );
    }

}
