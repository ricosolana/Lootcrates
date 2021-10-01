package com.crazicrafter1.lootcrates.DEPRECATED;

import com.crazicrafter1.crutils.Util;
import com.crazicrafter1.lootcrates.crate.loot.LootItem;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LootItemPotion extends LootItem {

    private final List<QPotionEffect> potionEffects;

    public LootItemPotion(Map<String, Object> args) {
        super(args);
        potionEffects = (List<QPotionEffect>) args.get("potionEffects");
    }

    public LootItemPotion(ItemStack visual, List<QPotionEffect> potionEffects) {
        super(visual, 1, 1);

        this.potionEffects = potionEffects;
    }

    @Override
    public ItemStack getIcon() {
        // iterate all potion effects
        ItemStack i = super.getIcon();
        PotionMeta potionMeta = (PotionMeta) i.getItemMeta();
        assert potionMeta != null;
        for (QPotionEffect effect : potionEffects) {
            potionMeta.addCustomEffect(effect.getBasedRandom(), true);
        }
        i.setItemMeta(potionMeta);
        return i;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();

        result.put("potionEffects", potionEffects);

        return result;
    }

    public class QPotionEffect implements ConfigurationSerializable {

        public final PotionEffectType potionEffectType;
        public final int minDuration;
        public final int maxDuration;
        public final int amplifier;

        public QPotionEffect(Map<String, Object> args) {
            potionEffectType = PotionEffectType.getByName((String)args.get("effect"));
            minDuration = (int) args.get("minDuration");
            maxDuration = (int) args.get("maxDuration");
            amplifier = (int) args.get("amplifier");
        }

        PotionEffect getBasedRandom() {
            return new PotionEffect(this.potionEffectType,
                    Util.randomRange(this.minDuration, maxDuration),
                    amplifier,
                    true, true);
        }

        @Override
        public Map<String, Object> serialize() {
            Map<String, Object> result = new LinkedHashMap<>();

            result.put("effect", potionEffectType.getName());
            result.put("minDuration", minDuration);
            result.put("maxDuration", maxDuration);
            result.put("amplifier", amplifier);

            return result;
        }
    }
}
