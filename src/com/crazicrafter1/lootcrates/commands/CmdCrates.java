package com.crazicrafter1.lootcrates.commands;

import com.crazicrafter1.lootcrates.Data;
import com.crazicrafter1.lootcrates.Editor;
import com.crazicrafter1.lootcrates.LootCratesAPI;
import com.crazicrafter1.lootcrates.Main;
import com.crazicrafter1.lootcrates.crate.Crate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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
            /*case "populate": {
                DefaultPopulator.populate();

                return feedback(sender, "Populating config with dummies");
            }
             */
            case "crate": {
                if (args.length < 3) return error(sender, "Input more arguments");

                Crate crate = LootCratesAPI.getCrateByID(args[1]);

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
            } case "reset": { // Save default config and load
                Main.get().saveDefaultConfig(true);
                // continue to load whatever is in config
                // no break intended
                feedback(sender, "Saved default config");
            } case "reload": { // Load saved config values
                Main.get().reloadConfig();
                return feedback(sender, "Loaded config from disk");
            }

            /* case "reload": {
                feedback(sender, "Reloading config...");
                plugin.reloadConfig();
                // save config somehow
                return feedback(sender, "Config was reloaded.");
            }*/ case "editor": {
                if (sender instanceof Player) {
                    // title, subtitle, fadein, stay, fadeout
                    Player p = (Player) sender;
                    if (!data.alertedPlayers.contains(p.getUniqueId())) {

                        p.sendTitle(ChatColor.RED + "Warning",
                                ChatColor.YELLOW + "Editor might be buggy",
                                5, 20 * 2, 5);

                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                p.resetTitle();
                                p.sendTitle(" ",
                                        ChatColor.YELLOW + "Submit bugs/requests to my Github",
                                        5, 20 * 2, 5);

                                p.sendMessage("" + ChatColor.DARK_GRAY + ChatColor.UNDERLINE + "https://github.com/PeriodicSeizures/LootCrates");

                                new BukkitRunnable() {
                                    @Override
                                    public void run() {



                                        p.resetTitle();
                                        p.sendTitle(" ",
                                                ChatColor.GOLD + "Constructive feedback is appreciated!",
                                                5, 20 * 2, 5);

                                        new BukkitRunnable() {
                                            @Override
                                            public void run() {
                                                //new MainMenu().show(p);
                                                Editor.open(p);
                                            }
                                        }.runTaskLater(plugin, 20 * 2 + 10);



                                    }
                                }.runTaskLater(plugin, 20 * 2 + 10);


                            }
                        }.runTaskLater(plugin, 20 * 2 + 10);

                    } else {
                        Editor.open(p);
                    }

                    data.alertedPlayers.add(p.getUniqueId());

                    return true;
                }

                return error(sender, "Can only be executed by a player");
            } case "version":
                return feedback(sender, "LootCrates version: " + plugin.getDescription().getVersion());
            /*case "flair": {
                if (!(sender instanceof Player p)) return error(sender, "Only a player can execute this argument");

                Util.giveItemToPlayer(p, new ItemBuilder(Material.CHEST).
                        name("&c&lLootcrates").lore(new String[]{ChatColor.ITALIC + "&8The way to reward players",
                                ChatColor.ITALIC + "&8in an awesome fashion"}).toItem());

                return feedback(sender, "You received 1 signature crate");
            }*//* case "detect": {
                if (!(sender instanceof Player p))
                    return error(sender, "Only a player can execute this argument");
                ItemStack itemStack = p.getInventory().getItemInMainHand();
                if (itemStack.getType() != Material.AIR) {
                    Crate crate = LootCratesAPI.extractCrateFromItem(itemStack);
                    if (crate != null) {
                        return feedback(sender, "Item is a crate (" + crate.id + ")");
                    } else {
                        return feedback(sender, "Item is a not a crate");
                    }
                }
                return error(sender, "Must hold an item to detect");
            }*/
            default:
                return error(sender, "Invalid initial argument");
        }
    }

}
