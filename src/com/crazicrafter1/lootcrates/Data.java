package com.crazicrafter1.lootcrates;

import com.crazicrafter1.lootcrates.crate.Crate;
import com.crazicrafter1.lootcrates.crate.LootGroup;
import org.bukkit.FireworkEffect;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Data implements ConfigurationSerializable {

    /*
     * Serializable stuff
     */
    public boolean debug = false;
    public boolean update = false;
    public int speed;

    public ItemStack unSelectedItem = null;
    public ItemStack selectedItem = null;
    public FireworkEffect fireworkEffect = null;

    public HashMap<String, Crate> crates = new HashMap<>();
    public HashMap<String, LootGroup> lootGroups = new HashMap<>();

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap();

        result.put("debug", debug);
        result.put("update", update);
        result.put("speed", speed);
        result.put("unSelectedItem", unSelectedItem);
        result.put("selectedItem", selectedItem);
        result.put("fireworkEffect", fireworkEffect);

        result.put("lootGroups", lootGroups);
        result.put("crates", crates);

        return result;
    }

    public static Data deserialize(Map<String, Object> args) {
        Data result = new Data();

        result.debug = (boolean) args.get("debug");
        result.update = (boolean) args.get("update");
        result.speed = (int) args.get("speed");

        result.unSelectedItem = (ItemStack) args.get("unSelectedItem");
        result.selectedItem = (ItemStack) args.get("selectedItem");
        result.fireworkEffect = (FireworkEffect) args.get("fireworkEffect");

        result.lootGroups = (HashMap<String, LootGroup>) args.get("lootGroups");
        result.crates = (HashMap<String, Crate>) args.get("crates");

        return result;
    }

}
