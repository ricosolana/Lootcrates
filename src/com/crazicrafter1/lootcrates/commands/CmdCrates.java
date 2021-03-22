package com.crazicrafter1.lootcrates.commands;

import com.crazicrafter1.guiapi.GraphicalAPI;
import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.lootcrates.Util;
import com.crazicrafter1.lootcrates.crate.Crate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CmdCrates extends CmdBase {

    public CmdCrates(Main plugin) {
        super(plugin, "lootcrates");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        // crates crate <type> <player> <count>
        if (args.length >= 3 && args[0].equalsIgnoreCase("crate")) {

            //if (Util.isNumeric(args[2])) return false;

            Crate crate = Crate.crateByID(args[1]);
            Player player = null;
            int count = 1;

            if (crate == null)
                return error(sender, "That crate doesn't exist");

            //player = Bukkit.getServer().getPlayer(args[2]);

            if (!args[2].equals("*") &&
                    ((player=Bukkit.getServer().getPlayer(args[2])) == null || !player.isOnline())) return error(sender, "That player cannot be found");

            // /crates crate legendary crazicrafter1 5
            if (args.length == 4) {
                if (Util.isNumeric(args[3])) {
                    count = Util.safeToInt(args[3]); //crateItem.setAmount(Util.safeToInt(args[3]));
                    if (count <= 0) return error(sender, "Count must be greater than 0");
                } else error(sender, "Invalid count");
            }

            if (!args[2].equals("*")) {
                player.getInventory().addItem(crate.getPreppedItemStack(true, count));
                return feedback(sender, "Gave " + count + " " + args[1] + " crate to " + ChatColor.GOLD + player.getName());
            } else {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.getInventory().addItem(crate.getPreppedItemStack(true, count));
                }
                return feedback(sender, "Gave " + count + " " + args[1] + " crate to all players (" + ChatColor.LIGHT_PURPLE + Bukkit.getOnlinePlayers().size() + ChatColor.GRAY + " online)");
            }


        }

        else if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            feedback(sender, "Reloading config...");
            Main.getInstance().reloadConfigValues();
            return feedback(sender, "ConfigWrapper was reloaded.");
        }

        else if (args.length == 1 && args[0].equalsIgnoreCase("editor")) {

            if (plugin.editor != null) {
                if (sender instanceof Player) {
                    GraphicalAPI.openMenu((Player) sender, plugin.editor.MAIN_MENU);
                    return true;
                }
                return error(sender, "Can only be executed by a player");
            } else return error(sender, "GraphicalAPI was not found or MC version is not 1.14+");
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

        plugin.info("LootCrates version: " + plugin.getDescription().getVersion());

        return false;
    }

}
