package com.crazicrafter1.lootcrates.crate.loot;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.crutils.Util;
import com.crazicrafter1.gapi.AbstractMenu;
import com.crazicrafter1.gapi.Result;
import com.crazicrafter1.gapi.TextMenu;
import com.crazicrafter1.lootcrates.Editor;
import com.crazicrafter1.lootcrates.ItemMutateMenuBuilder;
import com.crazicrafter1.lootcrates.crate.ActiveCrate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;

public class LootCommand implements ILoot {

    public String command;
    public ItemStack itemStack;

    public LootCommand() {
        command = "say %player_name% hi there";
        itemStack = new ItemBuilder(Material.CACTUS).name("Hello, world!").toItem();
    }

    public LootCommand(Map<String, Object> result) {
        command = (String) result.get("command");
        itemStack = (ItemStack) result.get("itemStack");
    }

    @Override
    public ItemStack getIcon(Player p) {
        return new ItemBuilder(itemStack).placeholders(p).toItem();
    }

    @Override
    public boolean execute(ActiveCrate activeCrate) {
        Bukkit.getServer().dispatchCommand(
                Bukkit.getConsoleSender(),
                Util.placeholders(activeCrate.getPlayer(), command)
        );
        return false;
    }

    @Override
    public String toString() {
        return "&7command: &f" + command;
    }

    @Override
    public AbstractMenu.Builder getMenuBuilder() {
        return new ItemMutateMenuBuilder()
                .build(itemStack, input -> this.itemStack = input)
                .title("LootCommand")
                .childButton(5, 2, () -> new ItemBuilder(Material.PAPER).name("&6Command").lore(Editor.LORE_LMB_EDIT).toItem(), new TextMenu.TBuilder()
                        .title("edit command", true)
                        .onClose((player, reroute) -> !reroute ? Result.BACK() : null)
                        .left(() -> command)
                        .right(() -> "Input the command (PAPI supported)")
                        .onComplete((player, s) -> {
                            if (!s.isEmpty()) {
                                this.command = s;
                                return Result.BACK();
                            }
                            return Result.TEXT("Invalid");
                        }));
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<>();

        result.put("command", command);
        result.put("itemStack", itemStack);

        return result;
    }
}
