package com.crazicrafter1.lootcrates;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

import static org.apache.commons.lang.Validate.notNull;

public class SkullUtil {

    public static ItemStack itemWithBase64(ItemStack item, String base64) {
        notNull(item, "item");
        notNull(base64, "base64");

        UUID hashAsId = new UUID(base64.hashCode(), base64.hashCode());
        return Bukkit.getUnsafe().modifyItemStack(item,
                "{SkullOwner:{Id:\"" + hashAsId + "\",Properties:{textures:[{Value:\"" + base64 + "\"}]}}}"
        );
    }

    public static ItemStack itemFromBase64(String base64) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD); //getPlayerSkullItem();
        return itemWithBase64(item, base64);
    }

    @Deprecated
    public static ItemStack getStormtrooper() {
        // Got this base64 string from minecraft-heads.com
        String base64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L" +
                "3RleHR1cmUvNTIyODRlMTMyYmZkNjU5YmM2YWRhNDk3YzRmYTMwOTRjZDkzMjMxYTZiNTA1YTEyY2U3Y2Q1MTM1YmE4ZmY5MyJ9fX0=";

        return itemFromBase64(base64);
    }

    @Deprecated
    public static ItemStack getStormtrooper(String headName) {
        // Got this base64 string from minecraft-heads.com
        //String base64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L" +
        //        "3RleHR1cmUvNTIyODRlMTMyYmZkNjU5YmM2YWRhNDk3YzRmYTMwOTRjZDkzMjMxYTZiNTA1YTEyY2U3Y2Q1MTM1YmE4ZmY5MyJ9fX0=";
        String base64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmQyOTgzOGM1ZWZhMzE0NDY5NTRhM2YzZGViNDczNTgyZDAyZmNhNjM5MWQyMzJjZWJkMzI5YjM4ZWVmZWEifX19";

        return ItemBuilder.builder(itemFromBase64(base64)).name(headName).toItem();
    }

}
