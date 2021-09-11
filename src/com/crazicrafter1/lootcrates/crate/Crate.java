package com.crazicrafter1.lootcrates.crate;

import com.crazicrafter1.lootcrates.*;
import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.crutils.ReflectionUtil;
import com.crazicrafter1.crutils.Util;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
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

    private final String name;
    private final ItemStack itemStack;
    private final String header;
    private final int size;
    private final int picks;
    private final Sound sound;
    private HashMap<LootGroup, Integer> lootGroups; // with partial summed weights
    private HashMap<LootGroup, Integer> originalWeights; // each individual original weight
    private ItemStack seasonalVariant;
    private int totalWeights;

    public Crate(String name, ItemStack itemStack, String header, int size, int picks, Sound sound) {
        this.name = name;
        this.itemStack = Crate.makeCrate(itemStack, name);
        this.header = header;
        this.size = size;
        this.picks = picks;
        this.sound = sound;
    }

    public void setLootGroups(HashMap<LootGroup, Integer> lootGroups) {
        lootGroups = sortByValue(lootGroups);
        this.originalWeights = (HashMap<LootGroup, Integer>) lootGroups.clone();

        int last = 0;
        for (Map.Entry<LootGroup, Integer> entry : lootGroups.entrySet()) {
            entry.setValue(last + entry.getValue());
            last = entry.getValue();
        }

        this.lootGroups = lootGroups;
        this.totalWeights = last;
    }

    /**
     * @return Crate name
     */
    public String getName() {
        return name;
    }

    public ItemStack getItemStack(int count) {
        if (Main.seasonal && this.seasonalVariant != null) {
            seasonalVariant.setAmount(count);
            return seasonalVariant;
        }
        ItemStack itemStack = new ItemStack(this.itemStack);

        itemStack.setAmount(count);
        return itemStack;
    }

    /**
     * @return Inventory name
     */
    public String getHeader() {
        return header;
    }

    /**
     * @return Inventory size
     */
    public int getSize() {
        return size;
    }

    /**
     * @return Loot selections
     */
    public int getPicks() {
        return picks;
    }

    /**
     * @return Click sound
     */
    public Sound getSound() {
        return sound;
    }

    public void prepSeasonalVariant() {
        ItemStack rawSeasonal = Seasonal.getSeasonalItem();
        if (rawSeasonal == null)
            seasonalVariant = null;
        else
            seasonalVariant = new ItemBuilder(Crate.makeCrate(rawSeasonal, name)).mergeLexicals(this.itemStack).toItem();
    }

    LootGroup getBasedRandom() {
        int rand = Util.randomRange(0, totalWeights-1);

        for (Map.Entry<LootGroup, Integer> entry : this.lootGroups.entrySet()) {
            if (entry.getValue() > rand) return entry.getKey();
        }

        return null;
    }

    public HashMap<LootGroup, Integer> getLootGroups() {
        return lootGroups;
    }
    public HashMap<LootGroup, Integer> getOriginalWeights() {
        return originalWeights;
    }

    private static ItemStack makeCrate(ItemStack itemStack, final String crate) {

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
            Class nbtTagCompoundClass = ReflectionUtil.getNMSClass("NBTTagCompound");
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
        return Main.crates.getOrDefault(id, null);
    }

    public static Crate crateByItem(final ItemStack itemStack) {

        /*
            Old code when custom name of the item was used to get crates
         */

        //ItemMeta meta = item.getItemMeta();
        //String name = meta.getDisplayName();
        //return Main.crates.getOrDefault(Main.crateNameIds.getOrDefault(name, null), null);

        if (itemStack.getType() == Material.AIR)
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

    public int getTotalWeights() {
        return totalWeights;
    }
}
