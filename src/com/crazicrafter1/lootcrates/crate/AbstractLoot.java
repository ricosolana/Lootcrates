package com.crazicrafter1.lootcrates.crate;

import com.crazicrafter1.lootcrates.ItemBuilder;
import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.lootcrates.Util;
import com.crazicrafter1.lootcrates.crate.loot.*;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Map;

public abstract class AbstractLoot {

    // base indicator, the count can change (upon cloning)
    private final ItemStack baseVisual;

    public AbstractLoot(ItemStack baseVisual) {
        this.baseVisual = baseVisual;
    }


    protected final ItemStack getBaseVisual() {
        return baseVisual.clone();
    }

    public abstract ItemStack getAccurateVisual();

    /**
     * Return whether the method did anything
     * (basic items and giving should return false for performance since visual is same resulting item)
     */
    public void perform(ActiveCrate activeCrate) {
        Util.giveItemToPlayer(activeCrate.getPlayer(), getAccurateVisual());
    }

    public final void setBaseVisualMeta(ItemMeta itemMeta) {
        baseVisual.setItemMeta(itemMeta);
    }

    public static AbstractLoot fromNewConfig(Map<String, Object> instance) {

        //Map<String, Object> instance = (config.getMapList(path).get(index);

        //config.getMapList;

        int min, max;

        if (instance.get("count") instanceof Integer) {
            min = (int)instance.get("count");
            max = min;
        } else {
            // read as range, string
            String[] split = ((String)instance.get("count")).replaceAll(" ", "").split(",");
            min = Integer.parseInt(split[0]);
            max = Integer.parseInt(split[1]);
        }

        if (instance.containsKey("item")) {

            String item = (String)instance.get("item");
            ItemBuilder builder = ItemBuilder.builder(Material.matchMaterial(item));

            if (instance.containsKey("name"))
                builder.name((String)instance.get("name"));


            if (instance.containsKey("lore"))
                builder.lore((List<String>) instance.get("lore"));


            {
                if (instance.containsKey("color") && item.contains("potion"))
                    builder.color((Color) instance.get("color"));
            }


            if (instance.containsKey("glow") && !instance.containsKey("enchantments") && !instance.containsKey("effects"))
                builder.glow(true);


            if (instance.containsKey("effects")) {
                LootPotionItem.QPotionEffect[] qEffects =
                        new LootPotionItem.QPotionEffect[((Map)instance.get("effects")).size()];

                // then read enchants
                List list = ((List)instance.get("effects"));

                // iterate the list
                for (int i=0; i<list.size(); i++) {

                    Map<String, Object> enchantMap = (Map<String, Object>) list.get(i);

                    PotionEffectType effect = PotionEffectType.getByName((String)enchantMap.get("effect"));
                    int amp = Integer.parseInt((String)enchantMap.get("amp"));

                    int emin, emax;

                    if (enchantMap.get("duration") instanceof Integer) {
                        emin = (int)enchantMap.get("duration");
                        emax = emin;
                    } else {
                        // read as range, string
                        String[] split = ((String)enchantMap.get("duration")).replaceAll(" ", "").split(",");
                        emin = Integer.parseInt(split[0]);
                        emax = Integer.parseInt(split[1]);
                    }

                    qEffects[i] = new LootPotionItem.QPotionEffect(effect, emin, emax, amp);
                }

                return new LootPotionItem(builder.toItem(), qEffects);
            }


            if (instance.containsKey("command")) {
                List<String> commands = (List<String>) instance.get("command");
                return new LootCommand(builder.toItem(), (String[]) commands.toArray());
            }


            if (instance.containsKey("enchantments")) {
                LootEnchantableItem.QEnchantment[] qEnchantments =
                        new LootEnchantableItem.QEnchantment[((Map)instance.get("enchantments")).size()];

                // then read enchants
                List list = ((List)instance.get("enchantments"));

                // iterate the list
                for (int i=0; i<list.size(); i++) {

                    Map<String, Object> enchantMap = (Map<String, Object>) list.get(i);

                    Enchantment enchantment = Util.matchEnchant((String)enchantMap.get("enchant"));

                    int emin, emax;

                    if (enchantMap.get("level") instanceof Integer) {
                        emin = (int)enchantMap.get("level");
                        emax = emin;
                    } else {
                        // read as range, string
                        String[] split = ((String)enchantMap.get("level")).replaceAll(" ", "").split(",");
                        emin = Integer.parseInt(split[0]);
                        emax = Integer.parseInt(split[1]);
                    }

                    qEnchantments[i] = new LootEnchantableItem.QEnchantment(enchantment, emin, emax);
                }

                return new LootEnchantableItem(builder.toItem(), qEnchantments);
            }

        } else if (instance.containsKey("qa") && Main.supportQualityArmory) {
            // qa item
            return new LootQA((String)instance.get("qa"), min, max);
        }  else if (instance.containsKey("crate")) {
            // crate
            Crate crate = Main.crates.get((String)instance.get("crate"));
            return new LootCrate(crate, min, max);
        } else {

            // throw?

        }
        return null;
    }



    public static AbstractLoot fromOldConfig(Map<String, Object> instance) {
        /*
            Will hold the value of the current path being tested in the case
            of an error, can print the path of issue
         */



        //ItemStack itemStack;



        int min = 1, max = min;


        if (instance.containsKey("count")) {
            min = (int)instance.get("count");
            max = min;
        } else if (instance.containsKey("range")) {
            String[] split = ((String)instance.get("range")).split(" ");
            min = Integer.parseInt(split[0]);
            max = Integer.parseInt(split[1]);
        }

        if (instance.containsKey("item")) {

            ItemBuilder builder = ItemBuilder.builder(Material.matchMaterial((String)instance.get("item")));

            if (instance.containsKey("name"))
                builder.name((String)instance.get("name"));


            if (instance.containsKey("lore"))
                builder.lore((List<String>) instance.get("lore"));


            if (instance.containsKey("command")) {
                List<String> commands = (List<String>) instance.get("command");
                return new LootCommand(builder.toItem(), (String[]) commands.toArray());
            }


            if (instance.containsKey("enchantments")) {
                Map<String, Map<String, Integer>> _enchantMap = ((Map<String, Map<String, Integer>>) instance.get("enchantments"));

                LootEnchantableItem.QEnchantment[] qEnchantments =
                        new LootEnchantableItem.QEnchantment[_enchantMap.size()];


                // iterate the entries
                int i = 0;
                for (String enchantKey : _enchantMap.keySet()) {
                    int level = _enchantMap.get(enchantKey).get("level");
                    Enchantment enchantment = Util.matchEnchant(enchantKey);

                    qEnchantments[i++] = new LootEnchantableItem.QEnchantment(enchantment, level, level);
                }

                return new LootEnchantableItem(builder.toItem(), qEnchantments);
            }

            return new LootItem(builder.toItem(), min, max);

        } else if (instance.containsKey("qa-item") && Main.supportQualityArmory) {
            // qa item
            String name = (String)instance.get("qa-item");
            return new LootQA(name, min, max);

        } else {
            //throw new Exception();
            // error

        }

        return null;

    }

}
