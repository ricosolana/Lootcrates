package com.crazicrafter1.lootcrates.crate;

import com.crazicrafter1.crutils.Bool;
import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.crutils.Util;
import com.crazicrafter1.lootcrates.crate.loot.*;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * TODO
 *  Abstract loot class is more or less abstracted config data
 *
 *  ActiveLoot is a loot that has calculated values upon being revealed
 *      - what to show on reveal
 *      - what to do on click
 */
public abstract class AbstractLoot {

    private final ItemStack icon;

    public AbstractLoot(ItemStack baseVisual) {
        this.icon = baseVisual;
    }

    public ItemStack getIcon() {
        return icon;
    }

    /*
     * do nothing by default
     */
    public abstract void execute(ActiveCrate activeCrate, boolean closed, Bool giveItem);



    @SuppressWarnings({"unchecked"})
    static AbstractLoot fromNewConfig(Map<String, Object> instance, EnumParseResult result) {

        int min = 1, max = 1;

        Object _count = instance.getOrDefault("count", null);
        result.code = EnumParseResult.Code.INVALID_COUNT;
        if (instance.get("count") instanceof Integer) {
            min = (int)instance.get("count");
            max = min;
        } else if (_count != null){
            // read as range, string
            String[] split = ((String)_count).replaceAll(" ", "").split(",");
            min = Integer.parseInt(split[0]);
            max = Integer.parseInt(split[1]);
        }

        if (instance.containsKey("item")) {
            result.code = EnumParseResult.Code.INVALID_ITEM;

            String item = (String)instance.get("item");
            ItemBuilder builder = new ItemBuilder(Material.matchMaterial(item));


            result.code = EnumParseResult.Code.INVALID_NAME;
            if (instance.containsKey("name"))
                builder.name((String)instance.get("name"));


            result.code = EnumParseResult.Code.INVALID_LORE;
            if (instance.containsKey("lore"))
                builder.lore((List<String>) instance.get("lore"));


            result.code = EnumParseResult.Code.INVALID_CUSTOMMODELDATA;
            if (instance.containsKey("model"))
                builder.customModelData((Integer) instance.get("model"));


            result.code = EnumParseResult.Code.INVALID_COMMAND;
            if (instance.containsKey("command")) {
                List<String> commands = (List<String>) instance.get("command");
                return new LootCommand(builder.toItem(), commands.toArray(new String[0]));
            }


            if (instance.containsKey("effects") && item.contains("potion")) {
                result.code = EnumParseResult.Code.INVALID_COLOR;
                if (instance.containsKey("color"))
                    builder.color(Util.matchColor((String) instance.get("color")));

                result.code = EnumParseResult.Code.INVALID_EFFECT_FORMAT;
                LootItemPotion.QPotionEffect[] qEffects =
                        new LootItemPotion.QPotionEffect[((List)instance.get("effects")).size()];

                // then read enchants
                //noinspection unchecked
                ArrayList<Object> list = (ArrayList<Object>) instance.get("effects");

                //result.code = Result.Code.INVALID_COUNT;
                // iterate the list
                for (int i=0; i<list.size(); i++) {

                    //noinspection unchecked
                    Map<String, Object> enchantMap = (Map<String, Object>) list.get(i);

                    result.code = EnumParseResult.Code.INVALID_EFFECT;
                    PotionEffectType effect = Util.matchPotionEffectType(((String)enchantMap.get("effect")));
                    if (effect == null)
                        throw new RuntimeException(new Exception());

                    result.code = EnumParseResult.Code.INVALID_EFFECT_AMPLIFIER;
                    int amp = (int)enchantMap.get("amp");

                    int emin, emax;

                    result.code = EnumParseResult.Code.INVALID_DURATION;
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

                    qEffects[i] = new LootItemPotion.QPotionEffect(effect, emin, emax, amp);
                }

                return new LootItemPotion(builder.toItem(), qEffects);
            }


            result.code = EnumParseResult.Code.INVALID_ENCHANT_FORMAT;
            if (instance.containsKey("enchantments")) {
                LootItemEnchantable.QEnchantment[] qEnchantments =
                        new LootItemEnchantable.QEnchantment[((List)instance.get("enchantments")).size()];

                // then read enchants
                List list = ((List)instance.get("enchantments"));

                // iterate the list
                for (int i=0; i<list.size(); i++) {

                    Map<String, Object> enchantMap = (Map<String, Object>) list.get(i);

                    result.code = EnumParseResult.Code.INVALID_ENCHANT;
                    Enchantment enchantment = Util.matchEnchant((String)enchantMap.get("enchant"));

                    if (enchantment == null)
                        throw new RuntimeException(new Exception());

                    int emin, emax;

                    result.code = EnumParseResult.Code.INVALID_LEVEL;
                    Object _level = enchantMap.get("level");
                    if (_level instanceof Integer) {
                        emin = (int)_level;
                        emax = emin;
                    } else {
                        String[] split = ((String)_level).replaceAll(" ", "").split(",");
                        emin = Integer.parseInt(split[0]);
                        emax = Integer.parseInt(split[1]);
                    }

                    qEnchantments[i] = new LootItemEnchantable.QEnchantment(enchantment, emin, emax);
                }

                return new LootItemEnchantable(builder.toItem(), qEnchantments);
            }

            result.code = EnumParseResult.Code.INVALID_GLOW;
            if (instance.containsKey("glow"))
                builder.glow(true);

            return new LootItem(builder.toItem(), min, max);

        } else if (instance.containsKey("qa") && Main.supportQualityArmory) {
            // qa item
            result.code = EnumParseResult.Code.INVALID_QA;
            return new LootItemQA((String)instance.get("qa"), min, max);
        }  else if (instance.containsKey("crate")) {
            // crate
            result.code = EnumParseResult.Code.INVALID_CRATE;
            String _crate = (String) instance.get("crate");
            Crate crate = Main.crates.get(_crate);
            return new LootItemCrate(crate, min, max);
        }

        result.code = EnumParseResult.Code.INVALID_LOOT;

        return null;
    }
}
