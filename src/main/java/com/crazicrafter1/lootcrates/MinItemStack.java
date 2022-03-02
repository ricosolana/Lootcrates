package com.crazicrafter1.lootcrates;

import com.crazicrafter1.crutils.*;
import com.crazicrafter1.lootcrates.Main;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.Indyuce.mmoitems.stat.PotionEffects;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class MinItemStack implements ConfigurationSerializable {

    //ItemStack itemStack;
    private ItemBuilder builder;

    private static final BiMap<String, Enchantment> ENCHANTMENTS = HashBiMap.create();
    private static final BiMap<String, PotionEffectType> EFFECTS = HashBiMap.create();

    static {
        for (Field field : Enchantment.class.getDeclaredFields()) {
            if (field.getType() == Enchantment.class) {
                ENCHANTMENTS.put(field.getName(), (Enchantment) ReflectionUtil.getFieldInstance(field, null));
            }
        }
        for (Field field : PotionEffectType.class.getDeclaredFields()) {
            if (field.getType() == PotionEffectType.class) {
                EFFECTS.put(field.getName(), (PotionEffectType) ReflectionUtil.getFieldInstance(field, null));
            }
        }
    }

    public MinItemStack(ItemStack itemStack) {
        this.builder = ItemBuilder.mutable(itemStack);
    }

    public MinItemStack(ItemBuilder builder) {
        this.builder = builder;
    }

    public MinItemStack(Map<String, Object> map) {
        try {
            builder = ItemBuilder.fromModernMaterial((String) map.get("type"))
                    .amount((int) map.getOrDefault("amount", 1))
                    .name((String) map.get("name"))
                    .lore((List<String>) map.get("lore"));

            String skull64 = (String) map.get("skull");
            if (skull64 != null) builder.skull(skull64);

            int model = (int) map.getOrDefault("model", -1);
            if (model != -1) builder.model(model);

            // will work for enchanted books too
            List<Map<String, Object>> enchantmentList = (List<Map<String, Object>>) map.get("enchantments");
            if (enchantmentList != null) {
                for (Map<String, Object> mapEntry : enchantmentList) {
                    String e = (String) mapEntry.get("name");
                    Enchantment enchantment = Objects.requireNonNull(ENCHANTMENTS.get(e), "Enchantment " + e + " does not exist");
                    int level = (int) mapEntry.getOrDefault("level", 1);
                    builder.enchant(enchantment, level);
                }
            }

            // potion
            List<Map<String, Object>> effectList = (List<Map<String, Object>>) map.get("effects");
            if (effectList != null) {
                for (Map<String, Object> mapEntry : effectList) {
                    String e = (String) mapEntry.get("name");
                    PotionEffectType effect = Objects.requireNonNull(EFFECTS.get(e), "PotionEffectType " + e + " does not exist");
                    Integer duration = (Integer) mapEntry.get("duration");
                    int amplifier = (int) mapEntry.getOrDefault("amplifier", 0);
                    if (duration != null) builder.effect(new PotionEffect(effect, duration, amplifier));
                }
            }

        } catch (Exception e) {
            Main.get().error("Error while instantiating ItemStackCfgWrapper: " + e.getMessage());
        }
    }

    public ItemBuilder get() {
        return builder;
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();

        Object temp;

        map.put("type", builder.getModernMaterial());
        if ((int)(temp = builder.getAmount()) > 1) map.put("amount", temp);
        if ((temp = builder.getName()) != null) map.put("name", ColorUtil.invert((String)temp));
        if ((temp = builder.getLoreList()) != null) map.put("lore", ((List<String>)temp).stream().map(ColorUtil::invert).collect(Collectors.toList()));

        if ((temp = builder.getSkull()) != null) map.put("skull", temp);
        if ((temp = builder.getEffects()) != null) map.put("model", temp);

        List<Map<String, Object>> subAdd = new ArrayList<>();
        BiMap<Enchantment, String> inverse1 = ENCHANTMENTS.inverse();
        for (Map.Entry<Enchantment, Integer> entry : builder.getEnchants().entrySet()) {
            Map<String, Object> sub = new LinkedHashMap<>();
            sub.put("name", inverse1.get(entry.getKey()));
            sub.put("level", entry.getValue());
            subAdd.add(sub);
        }
        if (!subAdd.isEmpty()) map.put("enchantments", subAdd);

        subAdd = new ArrayList<>();
        BiMap<PotionEffectType, String> inverse2 = EFFECTS.inverse();
        List<PotionEffect> effects = builder.getEffects();
        if (effects != null) for (PotionEffect effect : effects) {
            Map<String, Object> sub = new LinkedHashMap<>();
            sub.put("name", inverse2.get(effect.getType()));
            sub.put("amplifier", effect.getAmplifier());
            sub.put("duration", effect.getDuration());
            subAdd.add(sub);
        }
        if (!subAdd.isEmpty()) map.put("effects", subAdd);

        return map;
    }

}
