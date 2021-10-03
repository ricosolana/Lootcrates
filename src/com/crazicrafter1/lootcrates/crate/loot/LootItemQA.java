package com.crazicrafter1.lootcrates.crate.loot;

import me.zombie_striker.qg.api.QualityArmory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class LootItemQA extends LootOrdinateItem {

    public String name;

    /*
     * Default constructor
     */
    public LootItemQA() {
        super();
    }

    public LootItemQA(Map<String, Object> args) {
        super(
                QualityArmory.getCustomItemAsItemStack(
                        (String)args.get("name")),
                args);
        this.name = (String)args.get("name");
    }

    public LootItemQA(String name, int min, int max) {
        super(QualityArmory.getCustomItemAsItemStack(name), min, max);
        this.name = name;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();

        result.put("name", name);

        return super.serialize();
    }


}
