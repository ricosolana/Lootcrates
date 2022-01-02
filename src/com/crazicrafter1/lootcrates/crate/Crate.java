package com.crazicrafter1.lootcrates.crate;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.crutils.Util;
import com.crazicrafter1.lootcrates.LootCratesAPI;
import com.sun.istack.internal.NotNull;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Sound;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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

    // takes the weighted lootgroups and assigns the summed/cumulative weight lootgroups

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

    public String id;
    public ItemStack itemStack;
    public String title;
    public int columns;
    public int picks;
    public Sound sound;

    /// TODO stop using this
    // https://stackoverflow.com/questions/1761626/weighted-random-numbers
    /// The current implementation is tacky but fast and works
    /// The above proposed implementation is less tacky, but slightly slower due to an extra sum step

    ///             pros:       cons:
    /// current: fast           tacky   complex   memory
    /// propose: on the fly..

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
        //lootBySum = Stream.of(new Object[][] {{Main}}).collect(Collectors.toMap(data -> data[0], data -> data[1]));
    }

    public Crate(Map<String, Object> args) {
        //name = (String) args.get("name");
        //itemStack = Crate.makeCrate((ItemStack) args.get("itemStack"), name);
        // macro "lootset" for name or lore...

        title = Util.format((String) args.get("title"));
        columns = (int) args.get("columns");
        picks = (int) args.get("picks");
        sound = Sound.valueOf((String) args.get("sound"));
        // go ahead and sort to make sure no weirdness occurs
        //lootByName = sortByValue((LinkedHashMap<String, Integer>) args.get("lootGroups"));
        lootBySum = sortByValue((LinkedHashMap<LootSet, Integer>) args.get("weights"));

        /// TODO
        /// This will never work correctly when the crate is
        /// modified during runtime, since the values stay const (when using crate editor)
        /// a lambda will fix this
        /// using lambda will also require other kind of storage types
        /// use hashmap<macro, lambda> for correct use
        itemStack = (ItemStack) args.get("itemStack");
        //itemStack = new MacroItemBuilder((ItemStack) args.get("itemStack"))
        //        .macro("lootset_count", "" + lootBySum.size())
        //        .macro("crate_name", "" + id).toItem();
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
        ItemBuilder item = new ItemBuilder(itemStack);

        //String res = "%player_name%'s crate";
        //Main.get().info("res: " + PlaceholderAPI.setPlaceholders(p, res));

        return item
            .macro("%", "lootcrates_crate_picks", "" + picks)
            .macro("%", "lootcrates_crate_id", "" + id)
            .placeholders(p).toItem();
    }

    public String title(@NotNull Player p) {
        return PlaceholderAPI.setPlaceholders(p,
                Util.macro(title, "%", "lootset_count", "" + lootBySum.size()));
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
        //result.put("totalWeights", totalWeights);

        return result;
    }
}
