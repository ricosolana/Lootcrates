package com.crazicrafter1.lootcrates;

import com.crazicrafter1.lootcrates.crate.Crate;
import com.crazicrafter1.lootcrates.crate.LootSet;
import org.bukkit.FireworkEffect;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.util.*;

public class Data implements ConfigurationSerializable {

    public Data() {}

    public Data(Map<String, Object> args) {
        debug = (boolean) args.getOrDefault("debug", false);
        update = (boolean) args.getOrDefault("update", true);
        speed = (int) args.getOrDefault("speed", 4);

        unSelectedItem = (ItemStack) args.get("unSelectedItem");
        selectedItem = (ItemStack) args.get("selectedItem");

        // load in the same way, but need to pass name somehow
        lootSets = (LinkedHashMap<String, LootSet>) args.get("lootSets");
        for (Map.Entry<String, LootSet> entry : lootSets.entrySet()) {
            entry.getValue().id = entry.getKey();
        }

        crates = (LinkedHashMap<String, Crate>) args.get("crates");
        for (Map.Entry<String, Crate> entry : crates.entrySet()) {
            String id = entry.getKey();
            Crate crate = entry.getValue();

            crate.id = id;
            crate.itemStack = LootCratesAPI.makeCrate(crate.itemStack, id);

            // initialize weights
            crate.sumsToWeights();
        }

        fireworkEffect = (FireworkEffect) args.get("fireworkEffect");

        totalOpens = (int) args.getOrDefault("totalOpens", 0);

        try {
            File file = new File(Main.get().getDataFolder(), "players.csv");

            if (file.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(file));

                String line;
                while ((line = reader.readLine()) != null && !line.isEmpty()) {
                    alertedPlayers.add(UUID.fromString(line));
                }
                reader.close();
            }
        } catch (Exception e) {e.printStackTrace();}




        //for (String str : (List<String>) args.getOrDefault("alertedPlayers", new ArrayList<>())) {
        //    alertedPlayers.add(UUID.fromString(str));
        //}
        //alertedPlayers = (HashSet<UUID>) args.getOrDefault("alertedPlayers", new HashSet<>());
    }

    /*
     * Defaults, under the assumption that config permanently fails
     * Safe dev-like fallbacks
     */
    public boolean debug;
    public boolean update;
    public int speed;

    public ItemStack unSelectedItem;
    public ItemStack selectedItem;
    public FireworkEffect fireworkEffect;

    public HashMap<String, Crate> crates;
    public HashMap<String, LootSet> lootSets;

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

        result.put("lootSets", lootSets);

        result.put("crates", crates);

        result.put("fireworkEffect", fireworkEffect);

        result.put("totalOpens", totalOpens);


        if (!alertedPlayers.isEmpty())
            try {
                File file = new File(Main.get().getDataFolder(), "players.csv");

                BufferedWriter writer = new BufferedWriter(new FileWriter(file));

                for (UUID uuid : alertedPlayers) {
                    writer.write(uuid.toString());
                    writer.newLine();
                }
                writer.close();
            } catch (Exception e) {e.printStackTrace();}




        //List<String> uuids = new ArrayList<>();
        //for (UUID uuid : alertedPlayers) uuids.add(uuid.toString());
        //result.put("alertedPlayers", new ArrayList<>(uuids));

        return result;
    }
}
