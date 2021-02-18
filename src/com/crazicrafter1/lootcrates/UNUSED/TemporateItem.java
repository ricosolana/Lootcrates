package com.crazicrafter1.lootcrates.config;

import org.bukkit.Material;

public class TemporateItem {

    private Material material;
    private String name;
    private String[] lore;

    public TemporateItem(Material material, String name, String[] lore) {
        this.material = material;
        this.name = name;
        this.lore = lore;
    }



}
