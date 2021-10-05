package com.crazicrafter1.lootcrates;

import com.crazicrafter1.crutils.ReflectionUtil;
import com.crazicrafter1.gapi.Menu;
import com.crazicrafter1.lootcrates.crate.loot.AbstractLoot;
import com.crazicrafter1.lootcrates.crate.ActiveCrate;
import com.crazicrafter1.lootcrates.crate.Crate;
import com.crazicrafter1.lootcrates.crate.LootSet;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;
import java.util.HashMap;

public class LootCratesAPI {

    public static HashMap<Class<? extends AbstractLoot>, Class<? extends Menu>> behaviourMenus = new HashMap<>();

    public static void registerLoot(Class<? extends AbstractLoot> lootClass) {
        ConfigurationSerialization.registerClass(lootClass);
    }

    /**
     *
     * @param lootClass Register a ? extends AbstractLoot
     * @param menuClass Handler menu for instantiating the Loot (required constructor args: AbstractLoot, LootGroup)
     */
    public static void registerLoot(Class<? extends AbstractLoot> lootClass, Class<? extends Menu> menuClass) {
        behaviourMenus.put(lootClass, menuClass);
        ConfigurationSerialization.registerClass(lootClass);
    }

    public static void invokeMenu(AbstractLoot abstractLoot, LootSet lootGroup, Player p) {
        try {
            Class<? extends AbstractLoot> clazz = abstractLoot.getClass();

            ((Menu) ReflectionUtil.invokeConstructor(behaviourMenus.get(clazz),
                    clazz.cast(abstractLoot), lootGroup)).show(p);
        } catch (Exception e) {
            Main.get().error("Couldn't create AbstractLoot edit menu");
            if (Main.get().data.debug)
                e.printStackTrace();
        }
    }

    public static void invokeMenu(AbstractLoot abstractLoot, LootSet lootGroup, Player p, Class<? extends Menu> prevMenuClass) {
        try {
            Class<? extends AbstractLoot> clazz = abstractLoot.getClass();

            ((Menu) ReflectionUtil.invokeConstructor(behaviourMenus.get(clazz),
                    clazz.cast(abstractLoot), lootGroup, prevMenuClass)).show(p);
        } catch (Exception e) {
            Main.get().error("Couldn't create AbstractLoot edit menu");
            if (Main.get().data.debug)
                e.printStackTrace();
        }
    }

    public static Crate getCrateByID(String id) {
        return Main.get().data.crates.get(id);
    }

    public static Crate extractCrateFromItem(final ItemStack itemStack) {

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

        return getCrateByID(crateType);
    }

    /**
     * Opens a crate by id to a player
     */
    public static boolean openCrate(Player p, String name, int lock_slot) {
        Crate crate = getCrateByID(name);
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


}
