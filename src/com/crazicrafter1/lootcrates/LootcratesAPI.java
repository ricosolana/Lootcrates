package com.crazicrafter1.lootcrates;

import com.crazicrafter1.lootcrates.config.ConfigWrapper;
import com.crazicrafter1.lootcrates.crate.Crate;

public class LootcratesAPI {

    private static Main plugin;

    static void onEnable(Main plugin) {
        LootcratesAPI.plugin = plugin;
    }

    @Deprecated
    public static Crate getCrate(String id) {
        return plugin.config.crates.getOrDefault(id, null);
    }

    public static void registerCrate(Crate crate) {
        plugin.config.crates.put(crate.getId(), crate);
        //return plugin.config.crates.getOrDefault(id, null);
    }

    public static ConfigWrapper getConfig() {
        //me.zombie_striker.qg.api.QualityArmory.getGunByName("").getAccurateVisual();
        return plugin.config;
    }

}
