package com.crazicrafter1.lootcrates;

import com.crazicrafter1.lootcrates.crate.Crate;
import com.crazicrafter1.lootcrates.crate.LootGroup;
import org.bukkit.FireworkEffect;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Data implements Serializable {

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

}
