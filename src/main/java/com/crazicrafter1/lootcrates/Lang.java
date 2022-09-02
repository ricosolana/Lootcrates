package com.crazicrafter1.lootcrates;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.annotation.Nonnull;
import java.io.File;
import java.lang.reflect.Field;

public class Lang {
    // ideally a format like 'editor.button.crates' is best, but I want directly referred variables at the same time

    public static String Misc_OpenBug = "Either a player is cheating or plugin has bugged out\n" +
            "%s tried opening crate while already viewing crate\n" +
            "If the player is not cheating, contact dev";

    public static String Custom_Macros = "&9Custom macros: ";
    public static String Separate_Lore = "&eSeparate lore lines: ";

    public static String Editor_Title = "Editor";

    public static String ED_BTN_Crates = "&3&lCrates";
    public static String ED_BTN_LootSets = "&6&lLoot sets";
    public static String ED_BTN_Firework = "&e&lFirework";

    public static String ED_Crates_TI = "Crates";
    public static String ED_Crates_BTN_New = "&6New crate";
    public static String ED_Crates_New_TI = "New crate";

    public static String ED_LootSets_TI = "LootSets";
    public static String ED_LootSets_BTN_LORE = "&8%d items";
    public static String ED_LootSets_BTN_New = "&6New loot set";
    public static String ED_LootSets_New_TI = "New loot set";

    public static String ED_Firework_TI = "Firework";
    public static String ED_Firework_ERROR = "&eMust have effect";

    static final String IED_TI = "Edit item";
    static final String IED_Swap_ERROR = "Must swap with an item";
    static final String IED_BTN_Name = "&eName";
    static final String IED_Model_R = "Input an integer";

    public static String ED_LMB_EDIT = "&7LMB: &aEdit";
    public static String ED_LMB_TOGGLE = "&7LMB: &6Toggle";
    public static String ED_RMB_SHIFT_DELETE = "&7Shift-RMB: &cDelete";
    public static String ED_RMB_COPY = "&7RMB: &6Copy";
    public static String ED_LMB_DEC = "&7LMB: &c-";
    public static String ED_RMB_INC = "&7RMB: &a+";
    //public static String ED_MMB_TOGGLE = "&fMMB: &7toggle";
    public static String ED_SHIFT_MUL = "&7Shift: &r&7x5";
    public static String ED_NUM_SUM =       "&7NUM: 1   2   3   4";
    //public static String ED_NUM_SUM =       "     1   2   3   4";
    public static String ED_NUM_SUM_DESC =  "     &4-5 &c-1  &a+1 &2+5";

    public static String ED_Crates_PROTO_BTN_Columns = "&8&nColumns&r&8: &7%d";
    public static String ED_Crates_PROTO_BTN_Picks = "&8&nPicks&r&8: &7%d";
    public static String ED_Crates_PROTO_BTN_Sound = "&a&nSound&r&8: &7%s";
    public static String ED_Crates_PROTO_Sound_TI = "Sound";
    public static String ED_Crates_PROTO_Sound_R = "Input a sound";

    public static String ED_INVALID_ID = "Invalid id";
    public static String ED_DUP_ID = "That id already exists";

    public static String ED_LootSets_PROTO_BTN_Command = "&7Command: &f%s";
    public static String ED_LootSets_PROTO_Command_TI = "Edit command";
    public static String ED_LootSets_PROTO_Command_R = "Input the command";
    public static String SUPPORT_PLUGIN_X = "&6Supports %s";

    public static String ED_MIN = "&8&nMinimum";
    public static String ED_MAX = "&8&nMaximum";

    public static String ED_LootSets_PROTO_New_TI = "Add loot";

    public static String ERR_NO_CRATES = "No crates are registered";
    public static String ERR_NO_PERM_OPEN = "&cNo permission to open crate";
    public static String ERR_NO_PERM_PREVIEW = "&cNo permission to preview crate";

    public static String ASSIGN_EXACT = "Search or assign";

    public static String SET_BY_NAME = "&eSet item by name";
    public static String MMO_ENTER = "Enter as type:name";

    public static String LORE = "&bLore";

    public static String CUSTOM_MODEL = "&6Custom model";

    public static String MMO_FORMAT = "&8Format";
    public static String MMO_FORMAT_LORE = "&7 - exact level and tier\nrandom level and tier\nscale with player\nExample usage:";
    public static String MMO_EDIT_TIERS = "&8&lEdit tiers";

    public static String LOOT_MMO_EDIT_TITLE = "Edit scales";

    public static String ED_LootSets_PROTO_Skript_TI = "Skript event tag";
    public static String ED_LootSets_PROTO_Skript_R = "Input a tag";

    public static String ED_LootSets_PROTO_LootItem_LORE_Count = "&7Count: &f%d";
    public static String ED_LootSets_PROTO_LootItem_LORE_Range = "&7Range: &f[%d, %d]";

    public static String SPECIAL_FORMATTING = "&7&lSpecial formatting";

    public static String UNABLE_TO_CREATE = "Unable to create plugin data folder";
    public static String UNABLE_TO_METRICS = "Unable to enable bStats Metrics (%s)";
    public static String SKRIPT_INIT_ERROR = "Unable to init Skript addons";
    public static String SAVING_DEFAULT = "Saving default %s";
    public static String CONFIG_SAVING_FAILED = "Failed to save config: %s";
    public static String FAIL_REV_FILE = "Failed to read or delete rev file: %s";
    public static String RECOMMEND_CREATIVE = "Some editor features do not work properly outside of creative mode";
    public static String CONFIG_NULL_VALUE = "null value for '%s'";
    public static String CONFIG_ZERO_WEIGHT = "zero weight for '%s'";

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

    public static String CONFIG_Save = "Saving config...";
    public static String CONFIG_LOAD_FAIL = "Failed to load config: %s";
    public static String CONFIG_SaveError = "Failed to save config";
    public static String CONFIG_BackupError = "Failed to make a config backup";
    public static String CONFIG_DeleteDisabled = "Config purging is disabled";
    public static String CONFIG_DELETES = "Deleted %d old configurations";
    public static String NO_CONFIG_DELETES = "No configurations were deleted";
    public static String CONFIG_DELETES_FAIL = "Error deleting old backups: %s";

    public static String STATS_SAVE_FAIL = "Error saving player stats: %s";
    public static String STATS_LOAD_FAIL = "Error loading player stats: %s";

    public static String REWARDS_BACKUP = "Making a backup of rewards.yml";
    public static String REWARDS_1 = "Attempt 1: Loading rewards.yml";
    public static String REWARDS_2 = "Attempt 2: Loading default rewards.yml";
    public static String REWARDS_3 = "Attempt 3: Populating with default rewards";
    public static String REWARDS_SUCCESS = "Successfully loaded rewards.yml";
    public static String REWARDS_FAIL = "All fallback attempts failed";
    public static String REWARDS_REPORT = "Please report this at %s";

    private static final File PATH = new File(LCMain.get().getDataFolder(), "lang/");

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
                    if (field.getType() == String.class) {
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
