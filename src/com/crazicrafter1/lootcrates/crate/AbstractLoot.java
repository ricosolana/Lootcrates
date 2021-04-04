package com.crazicrafter1.lootcrates.crate;

import com.crazicrafter1.lootcrates.util.ItemBuilder;
import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.lootcrates.Result;
import com.crazicrafter1.lootcrates.util.Util;
import com.crazicrafter1.lootcrates.crate.loot.*;
import org.bukkit.Material;
import org.bukkit.configuration.MemorySection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
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

    public void perform(ActiveCrate activeCrate) {
        Util.giveItemToPlayer(activeCrate.getPlayer(), getAccurateVisual());
    }

    public final void setBaseVisualMeta(ItemMeta itemMeta) {
        baseVisual.setItemMeta(itemMeta);
    }

    @SuppressWarnings({"unchecked", "ConstantConditions"})
    static AbstractLoot fromNewConfig(Map<String, Object> instance, Result result) {

        //Map<String, Object> instance = (config.getMapList(path).get(index);

        //config.getMapList;

        int min = 1, max = 1;

        Object _count = instance.getOrDefault("count", null);
        result.code = Result.Code.INVALID_COUNT;
        if (instance.get("count") instanceof Integer) {
            min = (int)instance.get("count");
            max = min;
        } else if (_count != null){
            // read as range, string
            String[] split = ((String)_count).replaceAll(" ", "").split(",");
            min = Integer.parseInt(split[0]);
            max = Integer.parseInt(split[1]);
        }
        Main.getInstance().debug("" + min + " " + max);

        if (instance.containsKey("item")) {
            result.code = Result.Code.INVALID_ITEM;

            String item = (String)instance.get("item");
            ItemBuilder builder = ItemBuilder.builder(Material.matchMaterial(item));


            result.code = Result.Code.INVALID_NAME;
            if (instance.containsKey("name"))
                builder.name((String)instance.get("name"));


            result.code = Result.Code.INVALID_LORE;
            if (instance.containsKey("lore"))
                builder.lore((List<String>) instance.get("lore"));


            result.code = Result.Code.INVALID_COMMAND;
            if (instance.containsKey("command")) {
                Main.getInstance().debug("Command type");
                List<String> commands = (List<String>) instance.get("command");
                return new LootCommand(builder.toItem(), commands.toArray(new String[0]));
            }


            if (instance.containsKey("effects") && item.contains("potion")) {


                result.code = Result.Code.INVALID_COLOR;
                if (instance.containsKey("color"))
                    builder.color(Util.matchColor((String) instance.get("color")));


                result.code = Result.Code.INVALID_EFFECT_FORMAT;
                LootPotionItem.QPotionEffect[] qEffects =
                        new LootPotionItem.QPotionEffect[((List)instance.get("effects")).size()];

                // then read enchants
                //noinspection unchecked
                ArrayList<Object> list = (ArrayList<Object>) instance.get("effects");

                //result.code = Result.Code.INVALID_COUNT;
                // iterate the list
                for (int i=0; i<list.size(); i++) {

                    //noinspection unchecked
                    Map<String, Object> enchantMap = (Map<String, Object>) list.get(i);

                    result.code = Result.Code.INVALID_EFFECT;
                    PotionEffectType effect = PotionEffectType.getByName(((String)enchantMap.get("effect")).toUpperCase());
                    //PotionEffectTypeWrapper.
                    if (effect == null)
                        throw new RuntimeException(new Exception());

                    result.code = Result.Code.INVALID_EFFECT_AMPLIFIER;
                    int amp = (int)enchantMap.get("amp");

                    int emin, emax;

                    result.code = Result.Code.INVALID_DURATION;
                    Object _duration = enchantMap.get("duration");
                    if (_duration instanceof Integer) {
                        emin = (int)_duration;
                        emax = emin;
                    } else {
                        // read as range, string
                        String[] split = ((String)_duration).replaceAll(" ", "").split(",");
                        emin = Integer.parseInt(split[0]);
                        emax = Integer.parseInt(split[1]);
                    }

                    qEffects[i] = new LootPotionItem.QPotionEffect(effect, emin, emax, amp);
                }

                return new LootPotionItem(builder.toItem(), qEffects);
            }


            result.code = Result.Code.INVALID_ENCHANT_FORMAT;
            if (instance.containsKey("enchantments")) {
                LootEnchantableItem.QEnchantment[] qEnchantments =
                        new LootEnchantableItem.QEnchantment[((List)instance.get("enchantments")).size()];

                // then read enchants
                List list = ((List)instance.get("enchantments"));

                // iterate the list
                for (int i=0; i<list.size(); i++) {

                    Map<String, Object> enchantMap = (Map<String, Object>) list.get(i);

                    result.code = Result.Code.INVALID_ENCHANT;
                    //Enchantment enchantment = Enchantment.getByName((String)enchantMap.get("enchant"));
                    Enchantment enchantment = Util.matchEnchant((String)enchantMap.get("enchant"));

                    if (enchantment == null)
                        throw new RuntimeException(new Exception());

                    int emin, emax;

                    result.code = Result.Code.INVALID_LEVEL;
                    Object _level = enchantMap.get("level");
                    if (_level instanceof Integer) {
                        emin = (int)_level;
                        emax = emin;
                    } else {
                        // read as range, string
                        String[] split = ((String)_level).replaceAll(" ", "").split(",");
                        emin = Integer.parseInt(split[0]);
                        emax = Integer.parseInt(split[1]);
                    }

                    qEnchantments[i] = new LootEnchantableItem.QEnchantment(enchantment, emin, emax);
                }

                return new LootEnchantableItem(builder.toItem(), qEnchantments);
            }

            result.code = Result.Code.INVALID_GLOW;
            if (instance.containsKey("glow"))
                builder.glow(true);

            return new LootItem(builder.toItem(), min, max);

        } else if (instance.containsKey("qa") && Main.supportQualityArmory) {
            // qa item
            result.code = Result.Code.INVALID_QA;
            return new LootQA((String)instance.get("qa"), min, max);
        }  else if (instance.containsKey("crate")) {
            // crate
            result.code = Result.Code.INVALID_CRATE;
            String _crate = (String) instance.get("crate");
            Crate crate = Main.crates.get(_crate);
            Main.getInstance().debug("Crate-s: " + _crate + ", Crate is null: " + (crate == null));
            if (crate != null) {
                Main.getInstance().debug("" + crate.getItemStack(1).getItemMeta().getDisplayName());
            }
            for (Crate c : Main.crates.values()) {
                Main.getInstance().debug("c: " + c.getId());
            }
            return new LootCrate(crate, min, max);
        }

        result.code = Result.Code.INVALID_LOOT;

        return null;
    }



    @SuppressWarnings({"ConstantConditions", "unchecked"})
    static AbstractLoot fromOldConfig(MemorySection instance, Result result) {
        /*
            Will hold the value of the current path being tested in the case
            of an error, can print the path of issue
         */



        //ItemStack itemStack;



        int min = 1, max = min;

        result.code = Result.Code.INVALID_COUNT;
        if (instance.contains("count")) {
            min = (int)instance.get("count");
            max = min;
        } else if (instance.contains("range")) {
            result.code = Result.Code.INVALID_RANGE;
            String[] split = ((String)instance.get("range")).split(" ");
            min = Integer.parseInt(split[0]);
            max = Integer.parseInt(split[1]);
        }

        result.code = Result.Code.INVALID_ITEM;
        if (instance.contains("item")) {

            ItemBuilder builder = ItemBuilder.builder(Material.matchMaterial((String)instance.get("item")));


            result.code = Result.Code.INVALID_NAME;
            if (instance.contains("name"))
                builder.name((String)instance.get("name"));


            result.code = Result.Code.INVALID_LORE;
            if (instance.contains("lore"))
                builder.lore((List<String>) instance.get("lore"));


            result.code = Result.Code.INVALID_COMMAND;
            if (instance.contains("command")) {
                Main.getInstance().debug(instance.get("command").getClass().getName());
                ArrayList<String> commands = (ArrayList<String>) instance.get("command");

                String[] arr = commands.toArray(new String[0]);
                Main.getInstance().debug(arr[0]);
                return new LootCommand(builder.toItem(), arr);
            }



            result.code = Result.Code.INVALID_ENCHANT_FORMAT;
            if (instance.contains("enchantments")) {
                ArrayList<LootEnchantableItem.QEnchantment> qEnchantments = new ArrayList<>();
                MemorySection enchantSection = (MemorySection) instance.get("enchantments");

                //LootEnchantableItem.QEnchantment[] qEnchantments =
                //        new LootEnchantableItem.QEnchantment[enchantSection..size()];


                // iterate the entries
                result.code = Result.Code.INVALID_ENCHANT;
                //int i = 0;
                for (String enchantKey : enchantSection.getKeys(false)) {
                    int level = (int)((MemorySection)enchantSection.get(enchantKey)).get("level");
                    Enchantment enchantment = Util.matchEnchant(enchantKey);

                    //qEnchantments[i++] = new LootEnchantableItem.QEnchantment(enchantment, level, level);
                    qEnchantments.add(new LootEnchantableItem.QEnchantment(enchantment, level, level));
                }

                return new LootEnchantableItem(builder.toItem(), qEnchantments.toArray(
                        new LootEnchantableItem.QEnchantment[0]));
            }

            return new LootItem(builder.toItem(), min, max);

        } else if (instance.contains("qa-item") && Main.supportQualityArmory) {
            // qa item
            result.code = Result.Code.INVALID_QA;
            String name = (String)instance.get("qa-item");
            return new LootQA(name, min, max);

        }

        result.code = Result.Code.INVALID_LOOT;

        return null;

    }

}
