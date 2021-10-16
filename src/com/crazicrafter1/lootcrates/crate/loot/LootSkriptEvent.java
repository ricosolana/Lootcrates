package com.crazicrafter1.lootcrates.crate.loot;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.gapi.AbstractMenu;
import com.crazicrafter1.gapi.EnumResult;
import com.crazicrafter1.gapi.TextMenu;
import com.crazicrafter1.lootcrates.SkriptLootEvent;
import com.crazicrafter1.lootcrates.crate.ActiveCrate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;

public class LootSkriptEvent implements ILoot {

    String tag;
    ItemStack itemStack;

    public LootSkriptEvent() {
        tag = "awesome";
        itemStack = new ItemBuilder(Material.JUKEBOX).name("my tag").toItem();
    }

    public LootSkriptEvent(Map<String, Object> result) {
        // idk
        tag = (String) result.get("tag");
        itemStack = (ItemStack) result.get("itemStack");
    }

    @Override
    public ItemStack getIcon() {
        return itemStack;
    }

    @Override
    public boolean execute(ActiveCrate activeCrate) {
        Bukkit.getServer().getPluginManager().callEvent(new SkriptLootEvent(tag, activeCrate.getPlayer()));
        return false;
    }

    @Override
    public String toString() {
        return "&7tag: &f" + tag;
    }

    @Override
    public AbstractMenu.Builder getMenuBuilder() {
        return new TextMenu.TBuilder()
                .title("LootSkriptEvent tag edit")
                .left(() -> tag)
                .right(() -> "Input a tag")
                .onComplete((player, s) -> {
                    if (!s.isEmpty()) {
                        this.tag = s;
                        return EnumResult.BACK;
                    }
                    return EnumResult.TEXT("Invalid");
                });
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<>();

        result.put("tag", tag);
        result.put("itemStack", itemStack);

        return result;
    }
}
