package com.crazicrafter1.lootcrates.crate.loot;

import com.crazicrafter1.lootcrates.crate.AbstractLoot;
import com.crazicrafter1.lootcrates.crate.ActiveCrate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class LootCommand extends AbstractLoot {

    private final String[] commands;

    public LootCommand(ItemStack itemStack, String[] s) {
        super(itemStack);
        this.commands = s;
    }

    @Override
    public void perform(ActiveCrate activeCrate) {
        // execute commands
        Player p = activeCrate.getPlayer();
        for (String command : commands) {
            command = command.replaceAll("\\{player}", p.getName());

            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
        }
    }

    @Override
    public ItemStack getAccurateVisual() {
        return super.getBaseVisual();
    }

}
