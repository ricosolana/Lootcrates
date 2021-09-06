package com.crazicrafter1.lootcrates.crate.loot;

import com.crazicrafter1.lootcrates.util.Util;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class LootItemPotion extends LootItem {

    private final QPotionEffect[] potionEffects;

    public LootItemPotion(ItemStack visual, QPotionEffect[] potionEffects) {
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

    public record QPotionEffect(PotionEffectType potionEffectType, int minDuration, int maxDuration,
                                int amplifier) {

        PotionEffect getBasedRandom() {
            return new PotionEffect(this.potionEffectType,
                    Util.randomRange(this.minDuration, maxDuration),
                    amplifier,
                    true, true);
        }

    }
}
