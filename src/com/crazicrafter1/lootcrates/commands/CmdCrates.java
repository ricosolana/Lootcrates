package com.crazicrafter1.lootcrates.commands;

import com.crazicrafter1.crutils.ItemBuilder;
import com.crazicrafter1.crutils.Util;
import com.crazicrafter1.lootcrates.Data;
import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.lootcrates.crate.Crate;
import com.crazicrafter1.lootcrates.editor.MainMenu;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class CmdCrates extends CmdBase {

    private Data data = Main.get().data;

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

                if (!args[2].equals("*")) {
                    Player p = Bukkit.getServer().getPlayer(args[2]);

                    if (p == null)
                        return error(sender, "That player cannot be found");

                    p.getInventory().addItem(crate.itemStack);

                    return feedback(sender, "Gave a " + args[1] + " crate to " + ChatColor.GOLD + p.getName());
                } else {
                    for (Player p : players) {
                        p.getInventory().addItem(crate.itemStack);
                    }
                    return feedback(sender, "Gave a " + args[1] + " crate to all players (" + ChatColor.LIGHT_PURPLE + players.size() + ChatColor.GRAY + " online)");
                }
            } case "reload": {
                feedback(sender, "Reloading config...");
                plugin.reloadConfig();
                // save config somehow
                return feedback(sender, "Config was reloaded.");
            } case "editor": {
                if (sender instanceof Player p) {
                    // title, subtitle, fadein, stay, fadeout
                    if (!data.alertedPlayers.contains(p.getUniqueId())) {
                        p.sendTitle(ChatColor.RED + "Warning",
                                ChatColor.YELLOW + "Editor might have issues, be wary.",
                                10, 60, 10);
                        //feedback(sender, "The editor is not fully implemented, so if");
                        //feedback(sender, "a button does nothing, it does nothing!");
                        // coroutine
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                new MainMenu().show(p);
                            }
                        }.runTaskLater(plugin, 60);
                        data.alertedPlayers.add(p.getUniqueId());
                    } else {
                        new MainMenu().show(p);
                    }

                    return true;
                }

                return error(sender, "Can only be executed by a player");
            } case "version":
                return feedback(sender, "LootCrates version: " + plugin.getDescription().getVersion());
            case "flair": {
                if (!(sender instanceof Player p)) return error(sender, "Only a player can execute this argument");

                Util.giveItemToPlayer(p, new ItemBuilder(Material.CHEST).
                        name("&c&lLootcrates").lore(new String[]{ChatColor.ITALIC + "&8The way to reward players",
                                ChatColor.ITALIC + "&8in an awesome fashion"}).toItem());

                return feedback(sender, "You received 1 signature crate");
            } case "detect": {
                if (!(sender instanceof Player p))
                    return error(sender, "Only a player can execute this argument");
                ItemStack itemStack = p.getInventory().getItemInMainHand();
                if (itemStack.getType() != Material.AIR) {
                    Crate crate = Crate.crateByItem(itemStack);
                    if (crate != null) {
                        return feedback(sender, "Item is a crate (" + crate.name + ")");
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
