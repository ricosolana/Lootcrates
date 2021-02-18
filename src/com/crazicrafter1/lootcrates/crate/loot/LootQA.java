package com.crazicrafter1.lootcrates.crate.loot;

import me.zombie_striker.qg.api.QualityArmory;

public class LootQA extends LootItem {
    public LootQA(String name, int min, int max) {
        super(QualityArmory.getCustomItemAsItemStack(name), min, max);
    }
}
