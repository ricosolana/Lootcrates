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
        //Main.getInstance().important("ID: " + id);

        this.id = id;
        this.itemStack = Crate.makeCrate(itemStack, id);
    }

    public void setLootGroups(HashMap<LootGroup, Integer> lootGroups) {

        /*
            sort should return the map in an ordered way
         */

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
        for (Seasonal seasonal : Seasonal.values()) {
            if (seasonal.isToday()) {
                ItemStack head = seasonal.getHead();
                String finalName = seasonal.getPrefix() + " " + ChatColor.RESET + Objects.requireNonNull(itemStack.getItemMeta()).getDisplayName();

                ItemBuilder builder = ItemBuilder.builder(head).name(finalName);

                //seasonalVariant = asMarkedCrate(builder.toItem(), this.id);
                seasonalVariant = builder.toItem();
                return;
            }
        }

        seasonalVariant = null;
    }

    public LootGroup getBasedRandom() {



        int rand = Util.randomRange(0, chanceSum-1);

        for (Map.Entry<LootGroup, Integer> entry : this.lootGroups.entrySet()) {
            if (lootGroups.get(entry.getKey()) > rand) return entry.getKey();
        }

        return null;
    }

    public HashMap<LootGroup, Integer> getLootGroups() {
        return lootGroups;
    }

        }
        ItemStack copy = new ItemStack(itemStack);
        copy.setAmount(count);
        return copy;
    }


    public static ItemStack asMarkedCrate(ItemStack item, final String crate) {

        //net.minecraft.server.v1_14_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);

        Class<?> craftItemStackClass = ReflectionUtil.getCraftClass("inventory.CraftItemStack");

        Method asNMSCopyMethod = ReflectionUtil.getMethod(craftItemStackClass, "asNMSCopy", ItemStack.class);
        Object nmsStack = ReflectionUtil.invokeStaticMethod(asNMSCopyMethod, item);



        //NBTTagCompound nbt = nmsStack.getOrCreateTag(); //nmsStack.hasTag() ? nmsStack.getTag() : new NBTTagCompound();

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

    public static Crate matchCrate(ItemStack item) {

        //if (item == null) return null;

        //ItemMeta meta = item.getItemMeta();
        //if (meta == null) return null;

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



        return Main.crates.getOrDefault(crateType, null);
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
