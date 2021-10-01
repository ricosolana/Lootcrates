package com.crazicrafter1.lootcrates.serial;

import com.crazicrafter1.lootcrates.crate.Crate;
import com.crazicrafter1.lootcrates.crate.LootGroup;
import org.apache.commons.lang.Validate;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class SItemStack extends ItemStack implements Serializable {

    public SItemStack(Material type) {
        super(type, 1);
    }

    public SItemStack(ItemStack itemStack) {
        super(itemStack);
    }

    @Serial
    private void writeObject(ObjectOutputStream out) {
        try {
            out.writeObject(super.getType().name());
            out.writeObject(super.getAmount());
            //out.writeObject(super.getData());
            out.writeObject(super.getItemMeta());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Serial
    private void readObject(ObjectInputStream in) {
        try {
            setType(Material.matchMaterial((String)in.readObject()));
            setAmount(in.readInt());
            setItemMeta((in.readObject());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
