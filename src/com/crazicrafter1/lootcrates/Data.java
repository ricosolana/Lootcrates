package com.crazicrafter1.lootcrates;

import com.crazicrafter1.lootcrates.crate.Crate;
import com.crazicrafter1.lootcrates.crate.LootSet;
import org.bukkit.*;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Data implements ConfigurationSerializable {

    //public Data() {}

    public Data(Map<String, Object> args) {
        debug = (boolean) args.getOrDefault("debug", false);
        update = (boolean) args.getOrDefault("update", true);
        speed = (int) args.getOrDefault("speed", 4);

        unSelectedItem = (ItemStack) args.get("unSelectedItem");
        selectedItem = (ItemStack) args.get("selectedItem");

        // load in the same way, but need to pass name somehow
        lootGroups = (LinkedHashMap<String, LootSet>) args.get("lootSets");
        for (Map.Entry<String, LootSet> entry : lootGroups.entrySet()) {
            entry.getValue().id = entry.getKey();
        }

        crates = (LinkedHashMap<String, Crate>) args.get("crates");
        for (Map.Entry<String, Crate> entry : crates.entrySet()) {
            String id = entry.getKey();
            Crate crate = entry.getValue();

            crate.id = id;
            crate.itemStack = Crate.makeCrate(crate.itemStack, id);

            // initialize weights
            crate.sumsToWeights();
        }

        fireworkEffect = (FireworkEffect) args.get("fireworkEffect");

        totalOpens = (int) args.getOrDefault("totalOpens", 0);

        for (String str : (HashSet<String>) args.getOrDefault("alertedPlayers", new HashSet<>())) {
            alertedPlayers.add(UUID.fromString(str));
        }
        //alertedPlayers = (HashSet<UUID>) args.getOrDefault("alertedPlayers", new HashSet<>());
    }

    /*
     * Serializable stuff
     */
    public boolean debug;
    public boolean update;
    public int speed;

    public ItemStack unSelectedItem;
    public ItemStack selectedItem;
    public FireworkEffect fireworkEffect;

    public HashMap<String, Crate> crates;
    public HashMap<String, LootSet> lootGroups;

    public int totalOpens;

    public HashSet<UUID> alertedPlayers = new HashSet<>();

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<>();

        result.put("debug", debug);
        result.put("update", update);
        result.put("speed", speed);

        result.put("unSelectedItem", unSelectedItem);
        result.put("selectedItem", selectedItem);

        result.put("lootSets", lootGroups);
        Main.get().info(lootGroups.toString());

        result.put("crates", crates);

        result.put("fireworkEffect", fireworkEffect);

        result.put("totalOpens", totalOpens);

        //result.put("alertedPlayers", alertedPlayers);
        HashSet<String> uuids = new HashSet<>();
        for (UUID uuid : alertedPlayers) {
            uuids.add(uuid.toString());
        }
        result.put("alertedPlayers", uuids);

        return result;
    }
}
