package com.crazicrafter1.lootcrates;

import com.crazicrafter1.crutils.ReflectionUtil;
import com.crazicrafter1.gapi.Menu;
import com.crazicrafter1.lootcrates.crate.AbstractLoot;
import com.crazicrafter1.lootcrates.crate.LootGroup;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class LootCratesAPI {

    public static HashMap<Class<? extends AbstractLoot>, Class<? extends Menu>> behaviourMenus = new HashMap<>();

    /**
     *
     * @param lootClass Register a ? extends AbstractLoot
     * @param menuClass Handler menu for instantiating the Loot (required constructor args: AbstractLoot, LootGroup)
     */
    public static void registerLoot(Class<? extends AbstractLoot> lootClass, Class<? extends Menu> menuClass) {
        behaviourMenus.put(lootClass, menuClass);
        ConfigurationSerialization.registerClass(lootClass);
    }

    public static void invokeMenu(AbstractLoot abstractLoot, LootGroup lootGroup, Player p, Class<? extends Menu> prevMenuClass) {
        try {
            Class<? extends AbstractLoot> clazz = abstractLoot.getClass();

            ((Menu) ReflectionUtil.invokeConstructor(behaviourMenus.get(clazz),
                    clazz.cast(abstractLoot), lootGroup, prevMenuClass)).show(p);
        } catch (Exception e) {
            Main.getInstance().error("Couldn't create AbstractLoot edit menu");
            if (Data.debug)
                e.printStackTrace();
        }
    }


}
