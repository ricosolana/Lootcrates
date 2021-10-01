package com.crazicrafter1.lootcrates.crate;

import com.crazicrafter1.lootcrates.*;
import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.crutils.ReflectionUtil;
import com.crazicrafter1.crutils.Util;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

public class Crate implements ConfigurationSerializable {

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

    public String name;
    public ItemStack itemStack; // = Crate.makeCrate(itemStack, name);
    public String header;
    public int size;
    public int picks;
    public Sound sound;
    public LinkedHashMap<String, Integer> lootGroups; // sorted cumulative weights
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
        lootGroups = (LinkedHashMap<String, Integer>) args.get("lootGroups");
        for (Map.Entry<String, Integer> entry : lootGroups.entrySet()) {
            Main.getInstance().info("<" + entry.getKey() + ", " + entry.getValue() + ">");
        }
        Main.getInstance().info("");

        for (int weight : lootGroups.values())
            totalWeights = weight; // to force set to last one eventually
        Main.getInstance().info("totalWeights: " + totalWeights);
        //totalWeights = (int) args.get("totalWeights");
    }

    /**
     * Assumes that the map is cumulative-weight sorted in config
     * unknown whether config map retains original order
     */
    LootGroup getBasedRandom() {
        //  |     |     |  *  |
        //        markers
        // rand is an offset to look for in the

        //Main.getInstance().info("totalWeights: " + (totalWeights-1));

        int rand = Util.randomRange(0, totalWeights-1);

        Main.getInstance().info("rand: " + (rand));



        for (Map.Entry<String, Integer> entry : this.lootGroups.entrySet()) {
            //Main.getInstance().info("weight: " + (entry.getValue()));
            //Main.getInstance().info("s: " + entry.getKey());
            if (entry.getValue() > rand) return Main.DAT.lootGroups.get(entry.getKey());
        }

        Main.getInstance().info("returning null");

        return null;
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
        return Main.DAT.crates.get(id);
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
            Main.openCrates.put(p.getUniqueId(),
                    new ActiveCrate(p, crate, lock_slot));
            return true;
        }

        return false;
    }

    /**
     * Close a crate to a player
     */
    public static void closeCrate(Player p) {
        Main.openCrates.remove(p.getUniqueId()).close();
    }

    @Override
    public String toString() {
        return "itemStack: " + itemStack + "\n" +
                "header: " + header + "\n" +
                "size: " + header + "\n" +
                "picks: " + picks + "\n" +
                "sound: " + sound + "\n" +
                "lootGroups: " + lootGroups + "\n";
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
        result.put("lootGroups", lootGroups);
        //result.put("totalWeights", totalWeights);

        return result;
    }
}
