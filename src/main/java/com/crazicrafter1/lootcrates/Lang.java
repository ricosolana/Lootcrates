package com.crazicrafter1.lootcrates;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.lang.reflect.Field;

public class Lang {

    public static String CUSTOM_MACROS = "&9Custom macros: ";
    public static String SEPARATE_LORE = "&eSeparate lore lines: ";

    public static String TITLE_EDITOR = "Editor";

    public static String BUTTON_CRATES = "&3&lCrates";
    public static String TITLE_CRATES = "Crates";
    public static String BUTTON_NEW_CRATE = "&6New crate";
    public static String TITLE_NEW_CRATE = "New crate";
    public static String CRATE_FORMAT = "Format (strict):";
    public static String CRATE_FORMAT_LORE = "&7 - Lowercase\n&7 - Optional underscores";

    public static String BUTTON_LOOT_SETS = "&6&lLoot sets";
    public static String TITLE_LOOT_SETS = "Loot sets";
    public static String LOOT_SET_COUNT = "&8%d items";
    public static String BUTTON_NEW_LOOT_SET = "&6New loot set";
    public static String TITLE_NEW_LOOT_SET = "New loot set";

    public static String BUTTON_EDIT_FIREWORK = "&e&lFirework";
    public static String TITLE_FIREWORK = "Firework";
    public static String REQUIRE_FIREWORK_EFFECT = "&eMust have effect";



    static final String Edit_item = "Edit item";
    static final String PLACE_ITEM = "Must swap with an item";
    static final String NAME = "&eName";
    static final String Input_integer = "Input an integer";

    public static String LMB_EDIT = "&7LMB: &aEdit";
    public static String RMB_DELETE = "&7RMB: &cDelete";
    public static String LMB_DEC = "&7LMB: &c-";
    public static String RMB_INC = "&7RMB: &a+";
    public static String MMB_TOGGLE = "&fMMB: &7toggle";
    public static String SHIFT_MUL = "&7Shift: &r&7x5";
    public static String BUTTON_COLUMNS = "&8&nColumns&r&8: &7%d";
    public static String BUTTON_PICKS = "&8&nPicks&r&8: &7%d";
    public static String BUTTON_SOUND = "&a&nSound&r&8: &7%s";
    public static String TITLE_SOUND = "Sound";
    public static String INPUT_SOUND = "Input a sound";

    public static String DUPLICATE = "Invalid or duplicate";
    //public static String CRATE_ID = "&8id: %s";
    public static String LOOT_COMMAND = "&7Command: &f%s";
    public static String EDIT_COMMAND = "Edit command";
    public static String INPUT_COMMAND = "Input the command";
    public static String SUPPORT_PLUGIN_X = "&6Supports %s";

    public static String MINIMUM = "&8&nMinimum";
    public static String MAXIMUM = "&8&nMaximum";

    public static String ADD_LOOT = "Add loot";

    public static String ERR_NO_CRATES = "No crates are registered";

    public static String ASSIGN_EXACT = "Search or assign";

    public static String SET_BY_NAME = "&eSet item by name";
    public static String MMO_ENTER = "Enter as type:name";

    public static String LORE = "&bLore";

    public static String CUSTOM_MODEL = "&6Custom model";

    public static String MMO_FORMAT = "&8Format";
    public static String MMO_FORMAT_LORE = "&7 - exact level and tier\nrandom level and tier\nscale with player\nExample usage:";
    public static String MMO_EDIT_TIERS = "&8&lEdit tiers";

    public static String LOOT_MMO_EDIT_TITLE = "Edit scales";

    public static String SKRIPT_EVENT_TAG = "&2Event tag";
    public static String SKRIPT_INPUT_TAG = "Input a tag";

    public static String ITEM_COUNT = "&7Count: &f%d";
    public static String ITEM_RANGE = "&7Range: &f[%d, %d]";

    public static String SPECIAL_FORMATTING = "&7&lSpecial formatting";

    public static String LMB_NEW = "&7LMB: &6New";
    public static String NEW_LOOT_SET = "New LootSet";
    public static String FORMAT_ID = "&8id: %s";

    public static String UNKNOWN_REV = "Unable to determine plugin save revision";
    public static String ASSIGN_REV = "Must assign revision: %s";
    public static String VERSION = "Using version: %s";
    public static String REV = "Using revision: %d";
    public static String USAGE = "Usage: ";
    public static String ERR_ARG_UNKNOWN = "Unknown argument";
    public static String ERR_ARG_MORE = "Input more arguments: %s";
    public static String POPULATING = "Populating config with built-ins";
    public static String REV_UNSUPPORTED = "Revision %d is not yet implemented";
    public static String READ_W_REV = "Read config using revision %d";
    public static String REV_REQUIRE_INT = "Revision must be a integer (x>=0) or 'latest'";
    public static String READ_W_LATEST_REV = "Read config using latest revision (%d)";

    public static String CONFIG_SAVED = "Saved config to disk";
    public static String ERR_CRATE_UNKNOWN = "That crate doesn't exist";
    public static String ERR_PLAYER_CRATE = "You must be a player to give yourself a crate";
    public static String SELF_GIVE_CRATE = "Gave yourself 1 %s crate";
    public static String RECEIVE_CRATE = "You received 1 %s crate";
    public static String ERR_NONE_ONLINE = "No players online";
    public static String GIVE_CRATE_ALL = "Gave a %s crate to all players (%d online)";
    public static String ERR_PLAYER_UNKNOWN = "That player cannot be found";

    public static String CONFIG_LOADED_DEFAULT = "Loaded default config";
    public static String CONFIG_LOADED_DISK = "Loaded config from disk";

    public static String ERR_NEED_PLAYER = "Only a player can execute this command";

    public static String IS_CRATE = "Item is a crate (%s)";
    public static String NOT_CRATE = "Item is not a crate";
    public static String REQUIRE_HELD = "Must hold an item to detect";

    public static String TITLE = "Title";
    public static String EDIT_ITEM = "&8&nItemStack";
    public static String EDIT_TITLE = "&e&nTitle&r&e: %s";

    public static String EDIT_ICON = "&2Edit icon";

    public static String LOOT = "&6&nLoot";

    public static String ERR_INVALID = "Invalid input";

    public static String GAPI_REQUIRED = "Plugin Gapi required as a dependency";
    public static String GAPI_INSTALL = "Install it from here &n%s&r ";
    public static String GAPI_FAILED = "Gapi failed to enable";
    public static String JOIN_DISCORD = "Join the &8&lDiscord &rfor help and more &n%s&r ";

    public static String UPDATED = "Updated to %s";
    public static String RECOMMEND_RESTART = "Restarting is recommended to avoid issues";
    public static String LATEST_VERSION = "Using the latest version";
    public static String UPDATE_FAIL = "Error while updating";
    public static String UPDATE_AVAILABLE = "Update %s is available";

    public static String CONFIG_SAVE = "Saving config...";
    public static String CONFIG_LOAD_FAIL = "Failed to load config: %s";
    public static String CONFIG_SAVE_FAIL = "Failed to save config";
    public static String CONFIG_BACKUP_FAIL = "Failed to make a config backup";
    public static String CONFIG_PURGE_DISABLED = "Config purging is disabled";
    public static String CONFIG_PURGES = "Deleted %d old configurations";
    public static String NO_CONFIG_PURGES = "No configurations were deleted";
    public static String CONFIG_PURGES_FAIL = "Error deleting old backups: %s";

    public static String STATS_SAVE_FAIL = "Error saving player stats: %s";
    public static String STATS_LOAD_FAIL = "Error loading player stats: %s";

    public static String REWARDS_BACKUP = "Making a backup of rewards.yml";
    public static String REWARDS_1 = "Attempt 1: Loading rewards.yml";
    public static String REWARDS_2 = "Attempt 2: Loading default rewards.yml";
    public static String REWARDS_3 = "Attempt 3: Populating with default rewards";
    public static String REWARDS_SUCCESS = "Successfully loaded rewards.yml";
    public static String REWARDS_FAIL = "All fallback attempts failed";
    public static String REWARDS_REPORT = "Please report this at %s";

    public static final File langPath = new File(Main.get().getDataFolder(), "lang/");

    public static void load(String language) {
        try {
            langPath.mkdirs();
            File file = new File(langPath, language + ".yml");

            FileConfiguration config = new YamlConfiguration();
            config.load(file);

            for (String key : config.getKeys(false)) {
                // get field by that name
                try {
                    Field field = Lang.class.getDeclaredField(key);
                    if (field.getType() == String.class) {
                        field.set(null, config.getString(key).replace("\\n", "\n"));
                    }
                } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean save(String language, boolean override) {
        try {
            File file = new File(langPath, language + ".yml");
            if (override || !file.exists()) {

                langPath.mkdirs();

                FileConfiguration config = new YamlConfiguration();

                for (Field field : Lang.class.getDeclaredFields()) {
                    try {
                        if (field.getType() == String.class) {
                            config.set(field.getName(), ((String) field.get(null)).replace("\n", "\\n"));
                        }
                    } catch (Exception ignored) {
                    }
                }

                config.save(file);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

}
