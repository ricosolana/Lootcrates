package com.crazicrafter1.lootcrates.crate.loot;

import com.crazicrafter1.lootcrates.util.Util;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class LootPotionItem extends LootItem {

    private final QPotionEffect[] potionEffects;

    public LootPotionItem(ItemStack visual, QPotionEffect[] potionEffects) {
        super(visual, 1, 1);
        //super(new ItemStack(useType.material), 1);

        this.potionEffects = potionEffects;
    }


    //public LootPotion(Color color, boolean splash, QPotionEffect[] potionEffects) {
    //    super(null, 0);
    //    this.color = color;
    //    this.splash = splash;
    //    this.potionEffects = potionEffects;
    //}

    @Override
    public ItemStack getAccurateVisual() {
        // iterate all potion effects
        ItemStack i = getBaseVisual();
        PotionMeta potionMeta = (PotionMeta) i.getItemMeta();

        assert potionMeta != null;
        for (QPotionEffect effect : potionEffects) {
            potionMeta.addCustomEffect(effect.getBasedRandom(), true);
        }
        i.setItemMeta(potionMeta);
        return i;
    }

    public static class QPotionEffect {

        private final PotionEffectType potionEffectType;
        private final int minDuration;
        private final int maxDuration;
        private final int amplifier;

        public QPotionEffect(PotionEffectType potionEffectType,
                             int minDuration, int maxDuration,
                             int amplifier)
        {
            this.potionEffectType = potionEffectType;
            this.minDuration = minDuration;
            this.maxDuration = maxDuration;
            this.amplifier = amplifier;

        }

        PotionEffect getBasedRandom() {
            return new PotionEffect(this.potionEffectType,
                    Util.randomRange(this.minDuration, maxDuration),
                    amplifier,
                    true, true);
        }

    }
}
