package com.crazicrafter1.lootcrates.crate.loot;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.gapi.AbstractMenu;
import com.crazicrafter1.gapi.Result;
import com.crazicrafter1.gapi.TextMenu;
import com.crazicrafter1.lootcrates.Editor;
import com.crazicrafter1.lootcrates.ItemModifyMenu;
import com.crazicrafter1.lootcrates.sk.SkriptLootEvent;
import com.crazicrafter1.lootcrates.crate.ActiveCrate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;

public class LootSkriptEvent implements ILoot {

    public static final ItemStack EDITOR_ICON = ItemBuilder.copyOf(Material.MAP).name("&aAdd Skript tag...").build();

    String tag;
    ItemStack itemStack;

    public LootSkriptEvent() {
        tag = "awesome";
        itemStack = ItemBuilder.copyOf(Material.JUKEBOX).name("my tag").build();
    }

    public LootSkriptEvent(Map<String, Object> result) {
        // idk
        tag = (String) result.get("tag");
        itemStack = (ItemStack) result.get("itemStack");
    }

    @Override
    public ItemStack getIcon(Player p) {
        return ItemBuilder.copyOf(itemStack).placeholders(p).build();
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
        return new ItemModifyMenu()
                .build(itemStack, input -> this.itemStack = input)
                .title("LootSkriptEvent")
                .childButton(5, 2, () -> ItemBuilder.copyOf(Material.PAPER).name("&6Event tag").lore(Editor.LORE_LMB_EDIT).build(), new TextMenu.TBuilder()
                        .title("edit tag")
                        .onClose((player) -> Result.PARENT())
                        .leftRaw(() -> tag)
                        .right(() -> "Input a tag")
                        .onComplete((player, s) -> {
                            if (!s.isEmpty()) {
                                this.tag = s;
                                return Result.PARENT();
                            }
                            return Result.TEXT("Invalid");
                        }));
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<>();

        result.put("tag", tag);
        result.put("itemStack", itemStack);

        return result;
    }
}
