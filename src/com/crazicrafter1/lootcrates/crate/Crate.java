package com.crazicrafter1.lootcrates.crate;

import com.crazicrafter1.lootcrates.*;
import com.crazicrafter1.lootcrates.util.ItemBuilder;
import com.crazicrafter1.lootcrates.util.ReflectionUtil;
import com.crazicrafter1.lootcrates.util.Util;
import com.sun.istack.internal.NotNull;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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

    private final String id;
    private final ItemStack itemStack;
    private HashMap<LootGroup, Integer> lootGroups;
    private ItemStack seasonalVariant;
    private int chanceSum;

    public Crate(String id, ItemStack itemStack) {
        this.id = id;
        this.itemStack = Crate.makeCrate(itemStack, id);
    }

    public void setLootGroups(HashMap<LootGroup, Integer> lootGroups) {
        lootGroups = sortByValue(lootGroups);

        int last = 0;
        for (Map.Entry<LootGroup, Integer> entry : lootGroups.entrySet()) {
            entry.setValue(last + entry.getValue());
            last = entry.getValue();
        }

        this.lootGroups = lootGroups;
        this.chanceSum = last;
    }

    public String getId() {
        return id;
    }

    public void prepSeasonalVariant() {
        if (Main.seasonal)
            seasonalVariant = ItemBuilder.builder(Crate.makeCrate(Seasonal.getSeasonalItem(), id)).mergeLexicals(this.itemStack).toItem();
        else seasonalVariant = null;
    }

    LootGroup getBasedRandom() {
        int rand = Util.randomRange(0, chanceSum-1);

        for (Map.Entry<LootGroup, Integer> entry : this.lootGroups.entrySet()) {
            if (lootGroups.get(entry.getKey()) > rand) return entry.getKey();
        }

        return null;
    }

    public HashMap<LootGroup, Integer> getLootGroups() {
        return lootGroups;
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

    private static ItemStack makeCrate(ItemStack itemStack, final @NotNull String crate) {

        /*
            Back when I used CraftBukkit to 'easily' get and set NBT data, the comments below are the code
            that was used just for future reference
         */

        //net.minecraft.server.v1_14_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
        Class<?> craftItemStackClass = ReflectionUtil.getCraftClass("inventory.CraftItemStack");

        Method asNMSCopyMethod = ReflectionUtil.getMethod(craftItemStackClass, "asNMSCopy", ItemStack.class);
        Object nmsStack = ReflectionUtil.invokeStaticMethod(asNMSCopyMethod, itemStack);


        //NBTTagCompound nbt = nmsStack.getOrCreateTag();
        Method getOrCreateTagMethod = ReflectionUtil.getMethod(nmsStack.getClass(), "getOrCreateTag");
        Object nbt = ReflectionUtil.invokeMethod(getOrCreateTagMethod, nmsStack);


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

    public static Crate crateByID(String id) {
        return Main.crates.getOrDefault(id, null);
    }

    public static Crate crateByItem(final @NotNull ItemStack item) {

        /*
            Old code when custom name of the item was used to get crates
         */

        //ItemMeta meta = item.getItemMeta();
        //String name = meta.getDisplayName();
        //return Main.crates.getOrDefault(Main.crateNameIds.getOrDefault(name, null), null);




        //net.minecraft.server.v1_14_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
        Class<?> craftItemStackClass = ReflectionUtil.getCraftClass("inventory.CraftItemStack");

        Method asNMSCopyMethod = ReflectionUtil.getMethod(craftItemStackClass, "asNMSCopy", ItemStack.class);
        Object nmsStack = ReflectionUtil.invokeStaticMethod(asNMSCopyMethod, item);


        //NBTTagCompound nbt = nmsStack.getOrCreateTag();
        Method getOrCreateTagMethod = ReflectionUtil.getMethod(nmsStack.getClass(), "getOrCreateTag");
        Object nbt = ReflectionUtil.invokeMethod(getOrCreateTagMethod, nmsStack);


        //String crateType = nbt.getString("Crate");
        Method getStringMethod = ReflectionUtil.getMethod(nbt.getClass(), "getString", String.class);
        String crateType = (String) ReflectionUtil.invokeMethod(getStringMethod, nbt, "Crate");

        return Crate.crateByID(crateType);
    }

    /**
     * Opens a crate by id to a player
     */
    public static boolean openCrate(Player p, String id, int lock_slot) {
        if (!Main.openCrates.containsKey(p.getUniqueId())) {
            Crate crate = Main.crates.get(id);
            Main.openCrates.put(p.getUniqueId(), new ActiveCrate(p, crate, lock_slot));
            return true;
        }

        return false;
    }

    /**
     * Close a crate to a player
     */
    public static void closeCrate(Player p) {
        Main.openCrates.get(p.getUniqueId()).close();
        Main.openCrates.remove(p.getUniqueId());
    }
}
