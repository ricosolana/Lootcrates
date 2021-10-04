package com.crazicrafter1.lootcrates.crate;

import com.crazicrafter1.lootcrates.*;
import com.crazicrafter1.crutils.ReflectionUtil;
import com.crazicrafter1.crutils.Util;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
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
        for (Map.Entry<LootGroup, Integer> entry : lootBySum.entrySet()) {
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
        for (Map.Entry<LootGroup, Integer> entry : lootBySum.entrySet()) {
            int weight = entry.getValue() - prevSum;

            lootByWeight.put(entry.getKey(), weight);

            prevSum = entry.getValue();
        }

        this.totalWeights = prevSum;
    }

    public String name;
    public ItemStack itemStack;
    public String header;
    public int size;
    public int picks;
    public Sound sound;
    //public LinkedHashMap<String, Integer> lootByName;         // this will never be used beyond initialization
    public LinkedHashMap<LootGroup, Integer> lootBySum;
    public HashMap<LootGroup, Integer> lootByWeight;
    public int totalWeights;

    public Crate(String name, ItemStack itemStack, String header, int size, int picks, Sound sound) {
        this.name = name;
        this.itemStack = Crate.makeCrate(itemStack, name);
        this.header = header;
        this.size = size;
        this.picks = picks;
        this.sound = sound;
    }

    public Crate(Map<String, Object> args) {
        //name = (String) args.get("name");
        //itemStack = Crate.makeCrate((ItemStack) args.get("itemStack"), name);
        itemStack = (ItemStack) args.get("itemStack");
        header = (String) args.get("header");
        size = (int) args.get("size");
        picks = (int) args.get("picks");
        sound = Sound.valueOf((String) args.get("sound"));
        // go ahead and sort to make sure no weirdness occurs
        //lootByName = sortByValue((LinkedHashMap<String, Integer>) args.get("lootGroups"));
        lootBySum = sortByValue((LinkedHashMap<LootGroup, Integer>) args.get("lootGroups"));

        //Main.getInstance().info(lootBySum.toString());

    }

    /**
     * Assumes that the map is cumulative-weight sorted in config
     * unknown whether config map retains original order
     */
    LootGroup getBasedRandom() {
        int rand = Util.randomRange(0, totalWeights-1);

        for (Map.Entry<LootGroup, Integer> entry : this.lootBySum.entrySet()) {
            if (entry.getValue() > rand) return entry.getKey();
        }

        return null;
    }

    public String getFormattedPercent(LootGroup lootGroup) {
        return String.format("%.02f%%", 100.f * ((float) lootByWeight.get(lootGroup)/(float)totalWeights));
    }

    public String getFormattedFraction(LootGroup lootGroup) {
        return String.format("%d/%d", lootByWeight.get(lootGroup), totalWeights);
    }

    public static ItemStack makeCrate(final ItemStack itemStack, final String crate) {

        /*
            Back when I used CraftBukkit to 'easily' get and set NBT data, the comments below are the code
            that was used just for future reference
         */

        //net.minecraft.server.v1_14_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
        Class<?> craftItemStackClass = ReflectionUtil.getCraftClass("inventory.CraftItemStack");

        Method asNMSCopyMethod = ReflectionUtil.getMethod(craftItemStackClass, "asNMSCopy", ItemStack.class);
        Object nmsStack = ReflectionUtil.invokeStaticMethod(asNMSCopyMethod, itemStack);



        //NBTTagCompound nbt = nmsStack.getOrCreateTag();
        Method getOrCreateTagMethod = ReflectionUtil.getMethod(nmsStack.getClass(), "getTag");
        Object nbt = ReflectionUtil.invokeMethod(getOrCreateTagMethod, nmsStack);



        if (nbt == null) {
            Class nbtTagCompoundClass = ReflectionUtil.getNMClass("nbt.NBTTagCompound");
            Constructor nbtTagCompoundConstructor = ReflectionUtil.getConstructor(nbtTagCompoundClass);
            nbt = ReflectionUtil.invokeConstructor(nbtTagCompoundConstructor);
        }

        //nbt.setString("Crate", crate);
        Method setStringMethod = ReflectionUtil.getMethod(nbt.getClass(), "setString", String.class, String.class);
        ReflectionUtil.invokeMethod(setStringMethod, nbt, "Crate", crate);


        //nmsStack.setTag(nbt);
        Method setTagMethod = ReflectionUtil.getMethod(nmsStack.getClass(), "setTag", nbt.getClass());
        ReflectionUtil.invokeMethod(setTagMethod, nmsStack, nbt);


        //return CraftItemStack.asCraftMirror(nmsStack);
        Method asCraftMirrorMethod = ReflectionUtil.getMethod(craftItemStackClass, "asCraftMirror", nmsStack.getClass());

        return (ItemStack) ReflectionUtil.invokeStaticMethod(asCraftMirrorMethod, nmsStack);
    }

    public static Crate crateByName(String id) {
        return Main.get().data.crates.get(id);
    }

    public static Crate crateByItem(final ItemStack itemStack) {

        /*
            Old code when custom name of the item was used to get crates
         */

        if (itemStack == null || itemStack.getType() == Material.AIR)
            return null;

        //net.minecraft.server.v1_14_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
        Class<?> craftItemStackClass = ReflectionUtil.getCraftClass("inventory.CraftItemStack");

        Method asNMSCopyMethod = ReflectionUtil.getMethod(craftItemStackClass, "asNMSCopy", ItemStack.class);
        Object nmsStack = ReflectionUtil.invokeStaticMethod(asNMSCopyMethod, itemStack);


        // if player was holding nothing i guess
        //if (nmsStack == null)
        //    return null;


        //NBTTagCompound nbt = nmsStack.getOrCreateTag();
        Method getTagMethod = ReflectionUtil.getMethod(nmsStack.getClass(), "getTag");
        Object nbt = ReflectionUtil.invokeMethod(getTagMethod, nmsStack);

        if (nbt == null)
            return null;

        //String crateType = nbt.getString("Crate");
        Method getStringMethod = ReflectionUtil.getMethod(nbt.getClass(), "getString", String.class);
        String crateType = (String) ReflectionUtil.invokeMethod(getStringMethod, nbt, "Crate");

        return Crate.crateByName(crateType);
    }

    /**
     * Opens a crate by id to a player
     */
    public static boolean openCrate(Player p, String name, int lock_slot) {
        Crate crate = crateByName(name);
        if (crate != null) {
            Main.get().openCrates.put(p.getUniqueId(),
                    new ActiveCrate(p, crate, lock_slot));
            return true;
        }

        return false;
    }

    /**
     * Close a crate to a player
     */
    public static void closeCrate(Player p) {
        Main.get().openCrates.remove(p.getUniqueId()).close();
    }

    @Override
    public String toString() {
        return "itemStack: " + itemStack + "\n" +
                "header: " + header + "\n" +
                "size: " + header + "\n" +
                "picks: " + picks + "\n" +
                "sound: " + sound + "\n" +
                "lootGroups: " + lootBySum + "\n";
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<>();

        //result.put("name", name);
        result.put("itemStack", itemStack);
        result.put("header", header);
        result.put("size", size);
        result.put("picks", picks);
        result.put("sound", sound.name());
        result.put("lootGroups", lootBySum);
        //result.put("totalWeights", totalWeights);

        return result;
    }
}
