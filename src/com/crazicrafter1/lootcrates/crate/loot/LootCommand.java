package com.crazicrafter1.lootcrates.crate.loot;

import com.crazicrafter1.lootcrates.crate.AbstractLoot;
import com.crazicrafter1.lootcrates.crate.ActiveCrate;
import com.crazicrafter1.lootcrates.util.Bool;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@Deprecated
/**
 * Executing command are sketchy and should generally not be used
 */
public class LootCommand extends AbstractLoot {

    private final String[] commands;

    public LootCommand(ItemStack itemStack, String[] s) {
        super(itemStack);
        this.commands = s;
    }

    @Override
    public void execute(ActiveCrate activeCrate, boolean closed, Bool giveItem) {
        // execute commands
        Player p = activeCrate.getPlayer();
        for (String command : commands) {
            command = command.replaceAll("%player%", p.getName());

            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
        }
        giveItem.value = false;
    }

}
