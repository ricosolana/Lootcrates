package com.crazicrafter1.lootcrates.crate;

import com.crazicrafter1.lootcrates.ItemBuilder;
import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.lootcrates.Seasonal;
import com.crazicrafter1.lootcrates.Util;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public final class Crate {

    private static <T> HashMap<T, Integer> sortByValue(HashMap<T, Integer> hm)
    {
        // Create a list from elements of HashMap
        List<Map.Entry<T, Integer> > list =
                new LinkedList<>(hm.entrySet());

        // Sort the list
        list.sort(Comparator.comparing(Map.Entry::getValue));

        // put data from sorted list to hashmap
        HashMap<T, Integer> temp = new LinkedHashMap<>();
        for (Map.Entry<T, Integer> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }




    private final String id;
    //private String name;
    private final ItemStack itemStack;
    //private final HashMap<String, Integer> lootGroups;
    public final HashMap<LootGroup, Integer> lootGroups;
    private static ItemStack seasonalVariant;
    private final LootGroup[] sortedByChance;
    private final int chanceSum;

    public Crate(String id, ItemStack itemStack, HashMap<LootGroup, Integer> lootGroups) {
        this.id = id;
        this.itemStack = itemStack;
        this.lootGroups = lootGroups;

        int sum = 0;

        HashMap<String, Integer> namesAndChances = new HashMap<>();
        for (LootGroup key : lootGroups.keySet()) {
            namesAndChances.put(key.getName(), lootGroups.get(key));
            sum += lootGroups.get(key);
        }


        chanceSum = sum;


        HashMap<String, LootGroup> namesAndLootGroups = new HashMap<>();
        for (LootGroup key : lootGroups.keySet()) {
            namesAndLootGroups.put(key.getName(), key);
        }



        Map<String, Integer> hm1 = Crate.sortByValue(namesAndChances);

        int end = 0;
        this.sortedByChance = new LootGroup[lootGroups.size()];
        for (Map.Entry<String, Integer> en : hm1.entrySet()) {
            this.sortedByChance[end++] = namesAndLootGroups.get(en.getKey());
        }
    }

    public String getId() {
        return id;
    }

    public void prepSeasonalVariant() {
        for (Seasonal seasonal : Seasonal.values()) {
            if (seasonal.isToday()) {
                ItemStack head = seasonal.getHead();
                String finalName = seasonal.getPrefix() + " " + ChatColor.RESET + Objects.requireNonNull(itemStack.getItemMeta()).getDisplayName();

                ItemBuilder builder = ItemBuilder.builder(head).name(finalName);

                //seasonalVariant = markCrate(builder.toItem(), this.id);
                seasonalVariant = builder.toItem();
                return;
            }
        }

        seasonalVariant = null;
    }

    public LootGroup getBasedRandom() {
        int rand = Util.randomRange(0, chanceSum-1);

        for (LootGroup lootGroup : sortedByChance) {
            if (lootGroups.get(lootGroup) > rand) return lootGroup;
        }

        return null;
    }

    public void addChance(LootGroup lootGroup, int chance) {
        lootGroups.put(lootGroup, chance);
    }

    public HashMap<LootGroup, Integer> getLootGroups() {
        return lootGroups;
    }

    public ItemStack getPreppedItemStack(boolean useSeasonal) {

        Main.getInstance().debug("Crate#getPreppedItemStack() - ran");

        if (useSeasonal && seasonalVariant != null) {
            return seasonalVariant.clone();
        }
        return itemStack.clone();
    }

    public ItemStack getPreppedItemStack(boolean useSeasonal, int count) {
        if (useSeasonal && seasonalVariant != null) {
            ItemStack cloned = seasonalVariant.clone();
            cloned.setAmount(count);
            return cloned;
        }
        ItemStack cloned = itemStack.clone();
        cloned.setAmount(count);
        return cloned; //markCrate(this);
    }


    //private static ItemStack markCrate(@SuppressWarnings("unused") ItemStack item, @SuppressWarnings("unused") String crate) {
//
    //    // When CraftBukkit was used
    //    //net.minecraft.server.v1_14_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
    //    //NBTTagCompound nbt = nmsStack.getOrCreateTag(); //nmsStack.hasTag() ? nmsStack.getTag() : new NBTTagCompound();
    //    //nbt.setString("Crate", crate);
    //    //nmsStack.setTag(nbt);
    //    //return CraftItemStack.asCraftMirror(nmsStack);
//
    //    return null;
    //}

    public static Crate matchCrate(ItemStack item) {

        String name = Objects.requireNonNull(item.getItemMeta()).getDisplayName();
        return Main.crates.getOrDefault(name, null);

        //net.minecraft.server.v1_14_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
        //NBTTagCompound nbt = nmsStack.getOrCreateTag();
        //String crateType = nbt.getString("Crate");
        //return plugin.config.crates.getOrDefault(crateType, null);

    }

    /**
     * Assumes that crate by 'id' does exist
     */
    public static boolean openCrate(Player p, String id, int lock_slot) {
        if (!Main.openCrates.containsKey(p.getUniqueId())) {
            Crate crate = Main.crates.get(id);
            Main.openCrates.put(p.getUniqueId(), new ActiveCrate(p, crate, lock_slot));
            return true;
        }

        return false;
    }

    // assumes that player exists with open crate
    public static void closeCrate(Player p) {
        Main.openCrates.get(p.getUniqueId()).close();
    }
}
