package com.crazicrafter1.lootcrates.editor.loot;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.crutils.ReflectionUtil;
import com.crazicrafter1.gapi.Menu;
import com.crazicrafter1.gapi.ParallaxMenu;
import com.crazicrafter1.gapi.TriggerComponent;
import com.crazicrafter1.lootcrates.Data;
import com.crazicrafter1.lootcrates.LootCratesAPI;
import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.lootcrates.crate.AbstractLoot;
import com.crazicrafter1.lootcrates.crate.LootGroup;
import com.crazicrafter1.lootcrates.editor.MainMenu;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SingleAddLootMenu extends ParallaxMenu {

    //private static Map<String, Class<? extends ConfigurationSerializable>> aliases =
    //        (Map<String, Class<? extends ConfigurationSerializable>>) ReflectionUtil.getFieldInstance(
    //                ReflectionUtil.getField(ConfigurationSerialization.class, "aliases"),
    //                null);

    // A unique menu class is registered to handle each kind of abstract loot
    //public static HashMap<Class<? extends AbstractLoot>, Class<? extends Menu>> behaviourMenus = new HashMap<>();

    public SingleAddLootMenu(LootGroup lootGroup) {
        super("add loot");

        //for (Map.Entry<String, Class<? extends ConfigurationSerializable>> entry : aliases.entrySet()) {

        for (Map.Entry<Class<? extends AbstractLoot>, Class<? extends Menu>> entry : LootCratesAPI.behaviourMenus.entrySet()) {

            Class<? extends AbstractLoot> clazz = entry.getKey();

            addItem(new TriggerComponent() {
                @Override
                public void onLeftClick(Player p, boolean shift) {
                    // add this type

                    /*
                     * Will invoke the default constructor which is required for this to work
                     */
                    try {
                        LootCratesAPI.invokeMenu(
                                (AbstractLoot) ReflectionUtil.invokeConstructor(clazz), lootGroup, p, SingleAddLootMenu.class);
                    } catch (Exception e) {
                        Main.getInstance().error("Failed to invoke default loot menu");
                        if (Data.debug)
                            e.printStackTrace();
                    }
                }

                @Override
                public ItemStack getIcon() {
                    return new ItemBuilder(Material.GOLD_INGOT).name(clazz.getSimpleName()).toItem();
                }
            });
        }

        backButton(4, 5, BACK_1, LootMenu.class);
    }
}
