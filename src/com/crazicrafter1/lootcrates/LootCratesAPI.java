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
     * @param menuClass Handler menu for instantiating the Loot
     */
    public static void registerLoot(Class<? extends AbstractLoot> lootClass, Class<? extends Menu> menuClass) {
        behaviourMenus.put(lootClass, menuClass);
        ConfigurationSerialization.registerClass(lootClass);
    }

    public static void invokeMenu(Class<? extends AbstractLoot> lootClass, LootGroup lootGroup, Player p) {
        ((Menu)ReflectionUtil.invokeConstructor(behaviourMenus.get(lootClass), lootGroup)).show(p);
    }


}
