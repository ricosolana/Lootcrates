package com.crazicrafter1.lootcrates.editor.loot;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.crutils.ReflectionUtil;
import com.crazicrafter1.gapi.Menu;
import com.crazicrafter1.gapi.ParallaxMenu;
import com.crazicrafter1.gapi.TriggerComponent;
import com.crazicrafter1.lootcrates.LootCratesAPI;
import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.lootcrates.crate.AbstractLoot;
import com.crazicrafter1.lootcrates.crate.LootGroup;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class SingleAddTypeMenu extends ParallaxMenu {

    //private static Map<String, Class<? extends ConfigurationSerializable>> aliases =
    //        (Map<String, Class<? extends ConfigurationSerializable>>) ReflectionUtil.getFieldInstance(
    //                ReflectionUtil.getField(ConfigurationSerialization.class, "aliases"),
    //                null);

    // A unique menu class is registered to handle each kind of abstract loot
    //public static HashMap<Class<? extends AbstractLoot>, Class<? extends Menu>> behaviourMenus = new HashMap<>();

    public SingleAddTypeMenu(LootGroup lootGroup) {
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
                        // add the item
                        AbstractLoot loot = (AbstractLoot) ReflectionUtil.invokeConstructor(clazz);
                        lootGroup.loot.add(loot);
                        LootCratesAPI.invokeMenu(loot, lootGroup, p, SingleAddTypeMenu.class);
                    } catch (Exception e) {
                        Main.get().error("Failed to invoke default loot menu");
                        if (Main.get().data.debug)
                            e.printStackTrace();
                    }
                }

                @Override
                public ItemStack getIcon() {
                    return new ItemBuilder(Material.GOLD_INGOT).name(clazz.getSimpleName()).toItem();
                }
            });
        }

        backButton(4, 5, BACK_1, SingleLootGroupMenu.class, lootGroup);
    }
}
