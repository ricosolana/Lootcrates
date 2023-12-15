package com.crazicrafter1.lootcrates;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.annotation.Nonnull;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class Lang {
    // ideally a format like 'editor.button.crates' is best, but I want directly referred variables at the same time

    public static final File PATH = new File(LCMain.get().getDataFolder(), "lang/");
    public static String Misc_OpenBug = "A player is cheating or Lootcrates has malfunctioned\n" +
            "%s attempted to open crate while already open";
    public static String ASSIGN_EXACT = "Search or assign";
    public static String ASSIGN_REV = "Must assign revision: %s";
    public static String CONFIG_ERROR1 = "Failed to make a config backup";
    public static String CONFIG_DELETED_OLD = "Deleted %d old configurations";
    public static String CONFIG_ERROR2 = "Error deleting old backups: %s";
    public static String CONFIG_LOADED_DEFAULT = "Loaded default config";
    public static String CONFIG_LOADED_DISK = "Loaded config from disk";
    public static String CONFIG_ERROR3 = "Failed to load config: %s";
    public static String CONFIG_ERROR4 = "null value for '%s'";
    public static String CONFIG_SAVED = "Saved config to disk";
    public static String CONFIG_ERROR5 = "Failed to save config: %s";
    public static String CONFIG_SAVING = "Saving config...";
    public static String CONFIG_ERROR6 = "Failed to save config";
    public static String CONFIG_ERROR7 = "zero weight for '%s'";
    public static String EDITOR_ITEM_MODEL = "&6Custom model";
    public static String EDITOR_ITEM_MACROS = "&9Custom macros: ";
    public static String EDITOR_EDIT_ICON = "&2Edit icon";
    public static String EDITOR_EDIT_ITEM = "&8&nItemStack";
    public static String EDITOR_EDIT_TITLE = "&e&nTitle&r&e: %s";
    public static String EDITOR_CRATE = "&3&lCrates";
    public static String EDITOR_FIREWORK = "&e&lFirework";
    public static String EDITOR_LOOT = "&6&lLoot";
    public static String EDITOR_CRATE_NEW = "&6Add new crate";
    public static String EDITOR_CRATE_NEW_TITLE = "Name of crate:";
    public static String EDITOR_CRATE_COLUMNS = "&8&nColumns&r&8: &7%d";
    public static String EDITOR_CRATE_PICKS = "&8&nPicks&r&8: &7%d";
    public static String EDITOR_CRATE_SOUND = "&a&nSound&r&8: &7%s";
    public static String EDITOR_CRATE_SOUND_INPUT = "Input a sound";
    public static String EDITOR_CRATE_SOUND_TITLE = "Sound";
    public static String EDITOR_CRATE_TITLE = "Crates";
    public static String EDITOR_CRATE_ERROR1 = "That id already exists";
    public static String EDITOR_FIREWORK_TITLE = "Firework";
    public static String EDITOR_FIREWORK_ADD = "&6Add...";
    public static String EDITOR_FIREWORK_ERROR0 = "Color already applied";
    public static String EDITOR_FIREWORK_ERROR1 = "Hex must match #ABCDEF";
    public static String EDITOR_FIREWORK_COLOR_HINT = "#color";
    public static String EDITOR_FIREWORK_ERROR2 = "Too large";
    public static String EDITOR_FIREWORK_COLOR_NEW = "New color editor";
    public static String EDITOR_FIREWORK_ERROR3 = "Must start with #, 0x, ...";
    public static String EDITOR_FIREWORK_ERROR4 = "Unable to remove final element";
    public static String EDITOR_FIREWORK_NAME_COLORS = "&7Colors (&6%d&7)";
    public static String EDITOR_FIREWORK_NAME_FADE = "&7Fade (&6%d&7)";
    public static String EDITOR_FIREWORK_TITLE_COLORS = "Color editor";
    public static String EDITOR_FIREWORK_TITLE_FADE = "Fade editor";
    public static String EDITOR_FIREWORK_TYPE = "&7Type (&cLMB&7/&aRMB)";
    public static String EDITOR_ERROR9 = "Invalid: Ascii only";
    public static String EDITOR_LMB_DECREMENT = "&7LMB: &c-";
    public static String EDITOR_LMB_EDIT = "&7LMB: &aEdit";
    public static String EDITOR_LMB_TOGGLE = "&7LMB: &6Toggle";
    public static String EDITOR_LOOT_LORE = "&8%d items";
    public static String EDITOR_LOOT_NEW = "&6Add new collection...";
    public static String EDITOR_LOOT_ADD_TITLE = "Name of collection:";
    public static String EDITOR_LOOT_COMMAND = "&7Command: &f%s";
    public static String EDITOR_LOOT_COMMAND_INPUT = "Input the command";
    public static String EDITOR_LOOT_COMMAND_TITLE = "Edit command";
    public static String EDITOR_LOOT_COUNT = "&7Count: &f%d";
    public static String EDITOR_LOOT_RANGE = "&7Range: &f[%d, %d]";
    public static String EDITOR_LOOT_NEW_TITLE = "Add loot";
    public static String EDITOR_LOOT_SKRIPT_INPUT = "Input a tag";
    public static String EDITOR_LOOT_SKRIPT_TITLE = "Skript event tag";
    public static String EDITOR_LOOT_TITLE = "Loot";
    public static String EDITOR_COUNT_MAX = "&8&nMaximum";
    public static String EDITOR_COUNT_MIN = "&8&nMinimum";
    public static String EDITOR_COUNT_BINDS = "&7NUM: 1   2   3   4";
    public static String EDITOR_COUNT_CHANGE = "     &4-5 &c-1  &a+1 &2+5";
    public static String EDITOR_COPY = "&7RMB: &6Copy";
    public static String EDITOR_INCREMENT = "&7RMB: &a+";
    public static String EDITOR_DELETE = "&7Shift-RMB: &cDelete";
    public static String EDITOR_MULTIPLE = "&7Shift: &r&7x5";
    public static String COMMAND_ERROR_ARGS0 = "Input more arguments: %s";
    public static String COMMAND_ERROR_ARGS1 = "Unknown argument";
    public static String COMMAND_ERROR_CRATE = "That crate doesn't exist";
    public static String COMMAND_ERROR_INPUT = "Invalid input";
    public static String COMMAND_ERROR_PLAYER = "Only a player can execute this command";
    public static String COMMAND_ERROR_PLAYERS = "No players online";
    public static String COMMAND_ERROR_CRATES = "No crates are registered";
    public static String CRATE_ERROR_OPEN = "&cNo permission to open crate";
    public static String CRATE_ERROR_PREVIEW = "&cNo permission to preview crate";
    public static String CRATE_ERROR_PLAYER0 = "You must be a player to give yourself a crate";
    public static String CRATE_ERROR_PLAYER1 = "That player cannot be found";
    public static String EDITOR_TITLE = "Editor";
    public static String EDITOR_ID = "&8id: %s";
    public static String COMMAND_GIVE = "Gave a %s crate to %s";
    public static String COMMAND_GIVE_ALL = "Gave a %s crate to all players (%d online)";
    public static String COMMAND_IDENTIFY = "Item is a '%s' crate";
    public static String MESSAGE_DISCORD = "Join the &8&lDiscord &rfor help and more &n%s&r ";
    public static String MESSAGE_VERSION1 = "Using the latest version";
    public static String EDITOR_LMB_ADD = "&7LMB: &6New";
    public static String EDITOR_LOOT1 = "&6&nLoot";
    public static String LOOT_HELP_SUB = "&2" +
            "&7No more back-and-forth with menus!\n" +
            "&7This &conly &7works with basic items.\n" +
            "&7I do not recommend using with &2QA &7items\n" +
            "&7or custom items added from other plugins";
    public static String LOOT_HELP_TITLE = "&6Drop items anywhere here!";
    public static String LOOT_MMO_EDIT_TITLE = "Edit scales";
    public static String LORE = "&bLore";
    public static String MMO_EDIT_TIERS = "&8&lEdit tiers";
    public static String MMO_ENTER = "Enter as type:name";
    public static String MMO_FORMAT = "&8Format";
    public static String MMO_FORMAT_LORE = "&7 - exact level and tier\nrandom level and tier\nscale with player\nExample usage:";
    public static String NEW_LOOT = "&6Add item...";
    public static String MESSAGE_NOT_CRATE = "Item is not a crate";
    public static String MESSAGE_NO_CONFIG_DELETES = "No configurations were deleted";
    public static String MESSAGE_RECEIVE_CRATE = "You received 1 %s crate";
    public static String MESSAGE_EDITOR_OPEN = "Some editor features do not work properly outside of creative mode";
    public static String MESSAGE_REQUIRE_ITEM = "Must hold an item to detect";
    public static String MESSAGE_REV = "Using revision: %d";
    public static String MESSAGE_REWARDS_BACKUP = "Making a backup of rewards.yml";
    public static String MESSAGE_REWARDS_REPORT = "Please report this at %s";
    public static String MESSAGE_REWARDS_SUCCESS = "Successfully loaded rewards.yml";
    public static String MESSAGE_SAVING_DEFAULT = "Saving default %s";
    public static String EDITOR_ITEM_SET = "&eSet item by name";
    public static String MESSAGE_SKRIPT_ERROR1 = "Unable to init Skript addons";
    public static String EDITOR_FORMATTING = "&7&lSpecial formatting";
    public static String MESSAGE_STATS_ERROR11 = "Error loading player stats: %s";
    public static String MESSAGE_STATS_ERROR10 = "Error saving player stats: %s";
    public static String EDITOR_SUPPORTS = "&6Supports %s";
    public static String EDITOR_LORES = "&eSeparate lore lines: ";
    public static String EDITOR_TITLE1 = "Title";
    public static String FOLDER_ERROR = "Unable to create plugin data folder";
    public static String METRICS_ERROR = "Unable to enable bStats Metrics (%s)";
    public static String MESSAGE_UPDATE_AVAILABLE = "Update %s is available";
    public static String MESSAGE_COMMAND_USAGE = "Usage: ";
    public static String MESSAGE_VERSION = "Using version: %s";
    public static String EDITOR_NAME = "&eName";
    public static String EDITOR_ERROR_MODEL = "Input an integer";
    public static String EDITOR_ERROR_SWAP = "Must swap with an item";
    public static String EDITOR_EDIT_ITEM1 = "Edit item";

    public static boolean load(@Nonnull CommandSender sender, @Nonnull String language) {
        try {
            PATH.mkdirs();
            File file = new File(PATH, language + ".yml");

            YamlConfiguration config = new YamlConfiguration();
            config.load(file);

            for (String key : config.getKeys(false)) {
                // get field by that name
                try {
                    Field field = Lang.class.getDeclaredField(key);
                    if (!Modifier.isFinal(field.getModifiers()) && field.getType() == String.class) {
                        // in this case if the field from the config is not found, then
                        field.set(null, config.getString(key).replace("\\n", "\n"));
                    }
                } catch (NoSuchFieldException e) {
                    // field from config doesnt exist, meaning that
                    LCMain.get().notifier.commandSevere(sender, "Unknown language tag " + key);
                } catch (Exception ignored) {
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void save(@Nonnull CommandSender sender, @Nonnull String language, boolean overwrite) {
        try {
            File file = new File(PATH, language + ".yml");
            if (overwrite || !file.exists()) {
                PATH.mkdirs();

                YamlConfiguration config = new YamlConfiguration();

                for (Field field : Lang.class.getDeclaredFields()) {
                    try {
                        if (field.getType() == String.class) {
                            config.set(field.getName(), ((String) field.get(null)).replace("\n", "\\n"));
                        }
                    } catch (Exception ignored) {
                    }
                }

                config.save(file);
                LCMain.get().notifier.commandInfo(sender, String.format("Language file %s overwritten", language));
            } else {
                LCMain.get().notifier.commandInfo(sender, String.format("Language file %s was not overwritten", language));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
