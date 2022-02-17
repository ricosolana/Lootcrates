package com.crazicrafter1.lootcrates.crate;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.crutils.Util;
import com.crazicrafter1.lootcrates.LanguageUnit;
import com.crazicrafter1.lootcrates.LootCratesAPI;
import com.crazicrafter1.lootcrates.Main;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import org.bukkit.Sound;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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
    public ItemStack itemStack;
    public String title;
    public int columns;
    public int picks;
    public Sound sound;

    public LinkedHashMap<LootSet, Integer> lootBySum;
    public HashMap<LootSet, Integer> lootByWeight;
    public int totalWeights;

    public Crate(String id, ItemStack itemStack, String title, int columns, int picks, Sound sound) {
        this.id = id;
        this.itemStack = LootCratesAPI.makeCrate(itemStack, id);
        this.title = Util.format(title);
        this.columns = columns;
        this.picks = picks;
        this.sound = sound;
    }

    public Crate(Map<String, Object> args) {
        title = Util.format((String) args.get("title"));
        columns = (int) args.get("columns");
        picks = (int) args.get("picks");
        sound = Sound.valueOf((String) args.get("sound"));
        lootBySum = sortByValue((LinkedHashMap<LootSet, Integer>) args.get("weights"));
        itemStack = (ItemStack) args.get("itemStack");
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
    public ItemStack itemStack(@Nullable Player p) {
        ItemBuilder item = ItemBuilder.copyOf(itemStack);

        LanguageUnit dlu = Main.get().getLang(p);

        if (dlu == null) {
            return item
                    .macro("%", "lc_picks", "" + picks)
                    .macro("%", "lc_id", "" + id)
                    .macro("%", "lc_lscount", "" + lootBySum.size())
                    .placeholders(p).build();
        }

        Language clu = dlu.crates.get(id);

        return item
                .macro("%", "lc_picks", "" + picks)
                .macro("%", "lc_id", "" + id)
                .macro("%", "lc_lscount", "" + lootBySum.size())
                .placeholders(p)
                .name(clu.itemStackDisplayName)
                .lore(clu.itemStackLore)
                .build();
    }

    public String title(@NotNull Player p) {
        LanguageUnit dlu = Main.get().getLang(p);

        if (dlu == null) {
            return Util.placeholders(p, title);
        }

        Language clu = dlu.crates.get(id);

        return Util.placeholders(p, clu.title);
    }

    @Override
    public String toString() {
        return "itemStack: " + itemStack + "\n" +
                "title: " + title + "\n" +
                "size: " + title + "\n" +
                "picks: " + picks + "\n" +
                "sound: " + sound + "\n" +
                "weights: " + lootBySum + "\n";
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<>();

        //result.put("name", name);
        result.put("itemStack", itemStack);
        result.put("title", title);
        result.put("columns", columns);
        result.put("picks", picks);
        result.put("sound", sound.name());
        result.put("weights", lootBySum);

        return result;
    }
}
