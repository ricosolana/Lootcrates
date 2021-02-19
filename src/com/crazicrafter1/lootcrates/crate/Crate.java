package com.crazicrafter1.lootcrates.crate;

import com.crazicrafter1.lootcrates.ItemBuilder;
import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.lootcrates.Seasonal;
import com.crazicrafter1.lootcrates.Util;
import com.sun.istack.internal.Nullable;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.BufferedReader;
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
    private HashMap<LootGroup, Integer> lootGroups;
    private static ItemStack seasonalVariant;
    //private LootGroup[] sortedByChance;
    private int chanceSum;

    public Crate(String id, ItemStack itemStack) {
        this.id = id;
        this.itemStack = itemStack;
    }

    public void setLootGroups(HashMap<LootGroup, Integer> lootGroups) {

        /*
            sort should return the map in an ordered way
         */

        //Main.getInstance().debug("before chances:");
        //for (Map.Entry<LootGroup, Integer> entry : lootGroups.entrySet()) {
        //    Main.getInstance().debug("" + entry.getValue());
        //}


        lootGroups = sortByValue(lootGroups);
        //Main.getInstance().debug("sorted chances:");
        //for (Map.Entry<LootGroup, Integer> entry : lootGroups.entrySet()) {
        //    Main.getInstance().debug("" + entry.getValue());
        //}

        int last = 0;
        /*
            iterate values, summing the previous value to the current
         */
        for (Map.Entry<LootGroup, Integer> entry : lootGroups.entrySet()) {

            //lootGroups.put(entry.)
            entry.setValue(last + entry.getValue());
            last = entry.getValue();
        }

        //Main.getInstance().debug("after values:");
        //for (Map.Entry<LootGroup, Integer> entry : lootGroups.entrySet()) {
        //    Main.getInstance().debug("" + entry.getValue());
        //}

        this.lootGroups = lootGroups;
        this.chanceSum = last;

        //Main.getInstance().debug("sum: " + this.chanceSum);

        /*
            extensive test
         */

        //for (int i=0; i<200; i++) {
        //    LootGroup basedRandom = this.getBasedRandom();
        //    //if (basedRandom == null) {
        //    //Main.getInstance().debug("Error of null item");
        //    //Main.debug
        //    //    Scanner scanner = new Scanner(System.in);
        //    //    scanner.next();
        //    //}
        //}

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

        //Main.getInstance().debug("rand: " + rand);

        for (Map.Entry<LootGroup, Integer> entry : this.lootGroups.entrySet()) {
            if (lootGroups.get(entry.getKey()) > rand) return entry.getKey();
        }

        //for (LootGroup lootGroup : sortedByChance) {
        //    Main.getInstance().debug("" + ChatColor.GRAY + lootGroups.get(lootGroup));
        //    if (lootGroups.get(lootGroup) > rand) return lootGroup;
        //}

        //Main.getInstance().debug(ChatColor.RED + "null");

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

    public static Crate crateByID(String id) {
        return Main.crates.getOrDefault(id, null);
    }

    public static Crate matchCrate(ItemStack item) {

        if (item == null) return null;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;

        String name = meta.getDisplayName();
        return Main.crates.getOrDefault(Main.crateNameIds.getOrDefault(name, null), null);

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

    @Deprecated
    public static Crate fromConfig(String id) {
        FileConfiguration config = Main.getInstance().getConfig();

        String temp_path = "crates." + id;

        ItemBuilder builder = ItemBuilder.
                builder(Material.matchMaterial(config.getString(temp_path + ".item"))).
                name(config.getString(temp_path + ".name"));

        if (config.contains(temp_path + ".lore"))
            builder.lore(config.getStringList(temp_path + ".lore"));

        /*
        Map<String, Integer> lootgroupChances = (Map<String, Integer>) config.get(temp_path + ".chances");

        HashMap<LootGroup, Integer> lootGroups = new HashMap<>();
        for (String group : lootgroupChances.keySet()) {
            lootGroups.put(Main.lootGroups.get(group), lootgroupChances.get(group));
        }
        */

        // lootGroups

        return new Crate(id, builder.toItem());
    }

    // assumes that player exists with open crate
    public static void closeCrate(Player p) {
        Main.openCrates.get(p.getUniqueId()).close(false);
        Main.openCrates.remove(p.getUniqueId());
    }
}
