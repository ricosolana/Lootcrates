package com.crazicrafter1.lootcrates.crate;

import com.crazicrafter1.crutils.Util;
import com.crazicrafter1.lootcrates.LootCratesAPI;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Crate implements ConfigurationSerializable {

    // can be used to sort any given lootgroup map
    private static <T> LinkedHashMap<T, Integer> sortByValue(HashMap<T, Integer> hm) {
        // Create a list from elements of HashMap
        List<Map.Entry<T, Integer> > list =
                new LinkedList<>(hm.entrySet());

        // Sort the list
        list.sort(Comparator.comparing(Map.Entry::getValue));

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
    //public LinkedHashMap<String, Integer> lootByName;         // this will never be used beyond initialization
    // lootSet could be weakly referenced, basically WeakReference
    // or a c++ std::weak_ptr to Main.get().data.lootSets
    public LinkedHashMap<LootSet, Integer> lootBySum;
    public HashMap<LootSet, Integer> lootByWeight;
    public int totalWeights;

    public Crate(String id, ItemStack itemStack, String title, int columns, int picks, Sound sound) {
        this.id = id;
        this.itemStack = LootCratesAPI.makeCrate(itemStack, id);
        this.title = ChatColor.translateAlternateColorCodes('&', title);
        this.columns = columns;
        this.picks = picks;
        this.sound = sound;
        //lootBySum = Stream.of(new Object[][] {{Main}}).collect(Collectors.toMap(data -> data[0], data -> data[1]));
    }

    public Crate(Map<String, Object> args) {
        //name = (String) args.get("name");
        //itemStack = Crate.makeCrate((ItemStack) args.get("itemStack"), name);
        itemStack = (ItemStack) args.get("itemStack");
        title = ChatColor.translateAlternateColorCodes('&', (String) args.get("title"));
        columns = (int) args.get("columns");
        picks = (int) args.get("picks");
        sound = Sound.valueOf((String) args.get("sound"));
        // go ahead and sort to make sure no weirdness occurs
        //lootByName = sortByValue((LinkedHashMap<String, Integer>) args.get("lootGroups"));
        lootBySum = sortByValue((LinkedHashMap<LootSet, Integer>) args.get("weights"));

        //Main.getInstance().info(lootBySum.toString());

    }

    /**
     * Assumes that the map is cumulative-weight sorted in config
     * unknown whether config map retains original order
     */
    LootSet getBasedRandom() {
        int rand = Util.randomRange(0, totalWeights-1);

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
