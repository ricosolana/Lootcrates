package com.crazicrafter1.lootcrates.crate.loot;

import me.zombie_striker.qg.api.QualityArmory;

public class LootItemQA extends LootItem {
    public LootItemQA(String name, int min, int max) {
        super(QualityArmory.getCustomItemAsItemStack(name), min, max);
    }
}
