package com.crazicrafter1.lootcrates.commands;

import com.crazicrafter1.lootcrates.util.Int;
import com.crazicrafter1.lootcrates.util.Util;
import com.crazicrafter1.lootcrates.crate.Crate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CmdCrates extends CmdBase {

    public CmdCrates() {
        super("lootcrates");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        // crates crate <type> <player> <count>
        if (args.length == 0)
            return error(sender, "Input some arguments");

        switch (args[0].toLowerCase()) {
            case "crate": {
                if (args.length < 3) return error(sender, "Input more arguments");

                Crate crate = Crate.crateByName(args[1]);

                if (crate == null)
                    return error(sender, "That crate doesn't exist");

                int count = 1;

                // crates crate legendary crazicrafter1 5
                if (args.length == 4) {
                    Int wrapped = new Int();
                    if (Util.toInt(args[3], wrapped)) {
                        count = wrapped.value;
                        if (count <= 0) return error(sender, "Count must be greater than 0");
                    } else return error(sender, "Count must be numeric");
                }

                if (!args[2].equals("*")) {
                    Player p = Bukkit.getServer().getPlayer(args[2]);

                    if (p == null)
                        return error(sender, "That player cannot be found");

                    p.getInventory().addItem(crate.getItemStack(count));

                    //crate.crateByItem(crate.getItemStack(1));

                    return feedback(sender, "Gave " + count + " " + args[1] + " crate to " + ChatColor.GOLD + p.getName());
                } else {
                    for (Player p : players) {
                        p.getInventory().addItem(crate.getItemStack(count));
                    }
                    return feedback(sender, "Gave " + count + " " + args[1] + " crate to all players (" + ChatColor.LIGHT_PURPLE + players.size() + ChatColor.GRAY + " online)");
                }
            } case "reload": {
                feedback(sender, "Reloading config...");
                plugin.reloadConfigValues();
                return feedback(sender, "Config was reloaded.");
            } /*case "editor": {
                if (plugin.editor != null) {
                    if (sender instanceof Player) {
                        GraphicalAPI.openMenu((Player) sender, plugin.editor.MAIN_MENU);
                        return true;
                    }
                    return error(sender, "Can only be executed by a player");
                }
                return error(sender, "GraphicalAPI was not found or MC version is not 1.14+");
            }*/ case "version":
                return feedback(sender, "LootCrates version: " + plugin.getDescription().getVersion());
            case "flair":
                return error(sender, "Unimplemented");
            case "detect": {
                if (!(sender instanceof Player p))
                    return error(sender, "Only a player can execute this argument");
                ItemStack itemStack = p.getInventory().getItemInMainHand();
                if (itemStack.getType() != Material.AIR) {
                    Crate crate = Crate.crateByItem(itemStack);
                    if (crate != null) {
                        return feedback(sender, "Item is a crate (" + crate.getName() + ")");
                    } else {
                        return feedback(sender, "Item is a not a crate");
                    }
                }
                return error(sender, "Must hold an item to detect");
            } default:
                return error(sender, "Invalid initial argument");
        }

        /*
        else if (args.length == 0){

            if (!(sender instanceof Player)) return false;

            Player p = (Player)sender;

            ItemStack item = new ItemStack(Material.CHEST, 1);

            ArrayList<String> lores = new ArrayList<>(Arrays.asList(ChatColor.ITALIC + "&8The way to reward players", ChatColor.ITALIC + "&8in an awesome fashion"));

            Util.setName(item, "&c&lLootcrates");
            Util.setLore(item, lores);

            p.getInventory().addItem(item);

            return feedback(sender, "You received 1 signature crate");
        }
        */
    }

}
