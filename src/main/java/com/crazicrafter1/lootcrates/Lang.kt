package com.crazicrafter1.lootcrates

import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.lang.reflect.Modifier

object Lang {
    // ideally a format like 'editor.button.crates' is best, but I want directly referred variables at the same time
    val PATH: File = File(LCMain.Companion.get()!!.getDataFolder(), "lang/")
    var Misc_OpenBug = """
         A player is cheating or Lootcrates has malfunctioned
         %s attempted to open crate while already open
         """.trimIndent()
    var ASSIGN_EXACT = "Search or assign"
    var ASSIGN_REV = "Must assign revision: %s"
    var CONFIG_ERROR1 = "Failed to make a config backup"
    var CONFIG_DELETED_OLD = "Deleted %d old configurations"
    var CONFIG_ERROR2 = "Error deleting old backups: %s"
    var CONFIG_LOADED_DEFAULT = "Loaded default config"
    var CONFIG_LOADED_DISK = "Loaded config from disk"
    var CONFIG_ERROR3 = "Failed to load config: %s"
    var CONFIG_ERROR4 = "null value for '%s'"
    var CONFIG_SAVED = "Saved config to disk"
    var CONFIG_ERROR5 = "Failed to save config: %s"
    var CONFIG_SAVING = "Saving config..."
    var CONFIG_ERROR6 = "Failed to save config"
    var CONFIG_ERROR7 = "zero weight for '%s'"
    var EDITOR_ITEM_MODEL = "&6Custom model"
    var EDITOR_ITEM_MACROS = "&9Custom macros: "
    var EDITOR_EDIT_ICON = "&2Edit icon"
    var EDITOR_EDIT_ITEM = "&8&nItemStack"
    var EDITOR_EDIT_TITLE = "&e&nTitle&r&e: %s"
    var EDITOR_CRATE = "&3&lCrates"
    var EDITOR_FIREWORK = "&e&lFirework"
    var EDITOR_LOOT = "&6&lLoot"
    var EDITOR_CRATE_NEW = "&6Add new crate"
    var EDITOR_CRATE_NEW_TITLE = "Name of crate:"
    var EDITOR_CRATE_COLUMNS = "&8&nColumns&r&8: &7%d"
    var EDITOR_CRATE_PICKS = "&8&nPicks&r&8: &7%d"
    var EDITOR_CRATE_SOUND = "&a&nSound&r&8: &7%s"
    var EDITOR_CRATE_SOUND_INPUT = "Input a sound"
    var EDITOR_CRATE_SOUND_TITLE = "Sound"
    var EDITOR_CRATE_TITLE = "Crates"
    var EDITOR_CRATE_ERROR1 = "That id already exists"
    var EDITOR_FIREWORK_TITLE = "Firework"
    var EDITOR_FIREWORK_ADD = "&6Add..."
    var EDITOR_FIREWORK_ERROR0 = "Color already applied"
    var EDITOR_FIREWORK_ERROR1 = "Hex must match #ABCDEF"
    var EDITOR_FIREWORK_COLOR_HINT = "#color"
    var EDITOR_FIREWORK_ERROR2 = "Too large"
    var EDITOR_FIREWORK_COLOR_NEW = "New color editor"
    var EDITOR_FIREWORK_ERROR3 = "Must start with #, 0x, ..."
    var EDITOR_FIREWORK_ERROR4 = "Unable to remove final element"
    var EDITOR_FIREWORK_NAME_COLORS = "&7Colors (&6%d&7)"
    var EDITOR_FIREWORK_NAME_FADE = "&7Fade (&6%d&7)"
    var EDITOR_FIREWORK_TITLE_COLORS = "Color editor"
    var EDITOR_FIREWORK_TITLE_FADE = "Fade editor"
    var EDITOR_FIREWORK_TYPE = "&7Type (&cLMB&7/&aRMB)"
    var EDITOR_ERROR9 = "Invalid: Ascii only"
    var EDITOR_LMB_DECREMENT = "&7LMB: &c-"
    var EDITOR_LMB_EDIT = "&7LMB: &aEdit"
    var EDITOR_LMB_TOGGLE = "&7LMB: &6Toggle"
    var EDITOR_LOOT_LORE = "&8%d items"
    var EDITOR_LOOT_NEW = "&6Add new collection..."
    var EDITOR_LOOT_ADD_TITLE = "Name of collection:"

    //public static String EDITOR_LOOT_COMMAND = "&7Command: &f%s";
    //public static String EDITOR_LOOT_COMMANDS = "&7Commands: ";
    //public static String EDITOR_LOOT_COMMAND_INPUT = "Input the command";
    var EDITOR_LOOT_COMMAND_EDIT = "Edit commands"
    var EDITOR_LOOT_COMMAND_ADD = "&6Add command"
    var EDITOR_LOOT_COUNT = "&7Count: &f%d"
    var EDITOR_LOOT_RANGE = "&7Range: &f[%d, %d]"
    var EDITOR_LOOT_NEW_TITLE = "Add loot"
    var EDITOR_LOOT_SKRIPT_INPUT = "Input a tag"
    var EDITOR_LOOT_SKRIPT_TITLE = "Skript event tag"
    var EDITOR_LOOT_TITLE = "Loot"
    var EDITOR_COUNT_MAX = "&8&nMaximum"
    var EDITOR_COUNT_MIN = "&8&nMinimum"
    var EDITOR_COUNT_BINDS = "&7NUM: 1   2   3   4"
    var EDITOR_COUNT_CHANGE = "     &4-5 &c-1  &a+1 &2+5"
    var EDITOR_COPY = "&7RMB: &6Copy"
    var EDITOR_INCREMENT = "&7RMB: &a+"
    var EDITOR_DELETE = "&7Shift-RMB: &cDelete"
    var EDITOR_MULTIPLE = "&7Shift: &r&7x5"
    var COMMAND_ERROR_ARGS0 = "Input more arguments: %s"
    var COMMAND_ERROR_ARGS1 = "Unknown argument"
    var COMMAND_ERROR_CRATE = "That crate doesn't exist"
    var COMMAND_ERROR_INPUT = "Invalid input"
    var COMMAND_ERROR_PLAYER = "Only a player can execute this command"
    var COMMAND_ERROR_PLAYERS = "No players online"
    var COMMAND_ERROR_CRATES = "No crates are registered"
    var CRATE_ERROR_OPEN = "&cNo permission to open crate"
    var CRATE_ERROR_PREVIEW = "&cNo permission to preview crate"
    var CRATE_ERROR_PLAYER0 = "You must be a player to give yourself a crate"
    var CRATE_ERROR_PLAYER1 = "That player cannot be found"
    var EDITOR_TITLE = "Editor"
    var EDITOR_ID = "&8id: %s"
    var COMMAND_GIVE = "Gave a %s crate to %s"
    var COMMAND_GIVE_ALL = "Gave a %s crate to all players (%d online)"
    var COMMAND_IDENTIFY = "Item is a '%s' crate"
    var MESSAGE_DISCORD = "Join the &8&lDiscord &rfor help and more &n%s&r "
    var MESSAGE_VERSION1 = "Using the latest version"
    var EDITOR_LMB_ADD = "&7LMB: &6New"
    var EDITOR_LOOT1 = "&6&nLoot"
    var LOOT_HELP_SUB = """
         &2&7No more back-and-forth with menus!
         &7This &conly &7works with basic items.
         &7I do not recommend using with &2QA &7items
         &7or custom items added from other plugins
         """.trimIndent()
    var LOOT_HELP_TITLE = "&6Drop items anywhere here!"
    var LOOT_MMO_EDIT_TITLE = "Edit scales"
    var LORE = "&bLore"
    var MMO_EDIT_TIERS = "&8&lEdit tiers"
    var MMO_ENTER = "Enter as type:name"
    var MMO_FORMAT = "&8Format"
    var MMO_FORMAT_LORE = "&7 - exact level and tier\nrandom level and tier\nscale with player\nExample usage:"
    var NEW_LOOT = "&6Add item..."
    var MESSAGE_NOT_CRATE = "Item is not a crate"
    var MESSAGE_NO_CONFIG_DELETES = "No configurations were deleted"
    var MESSAGE_RECEIVE_CRATE = "You received 1 %s crate"
    var MESSAGE_EDITOR_OPEN = "Some editor features do not work properly outside of creative mode"
    var MESSAGE_REQUIRE_ITEM = "Must hold an item to detect"
    var MESSAGE_REV = "Using revision: %d"
    var MESSAGE_REWARDS_BACKUP = "Making a backup of rewards.yml"
    var MESSAGE_REWARDS_REPORT = "Please report this at %s"
    var MESSAGE_REWARDS_SUCCESS = "Successfully loaded rewards.yml"
    var MESSAGE_SAVING_DEFAULT = "Saving default %s"
    var EDITOR_ITEM_SET = "&eSet item by name"
    var MESSAGE_SKRIPT_ERROR1 = "Unable to init Skript addons"
    var EDITOR_FORMATTING = "&7&lSpecial formatting"
    var MESSAGE_STATS_ERROR11 = "Error loading player stats: %s"
    var MESSAGE_STATS_ERROR10 = "Error saving player stats: %s"
    var EDITOR_SUPPORTS = "&6Supports %s"
    var EDITOR_LORES = "&eSeparate lore lines: "
    var EDITOR_TITLE1 = "Title"
    var FOLDER_ERROR = "Unable to create plugin data folder"
    var METRICS_ERROR = "Unable to enable bStats Metrics (%s)"
    var MESSAGE_UPDATE_AVAILABLE = "Update %s is available"
    var MESSAGE_COMMAND_USAGE = "Usage: "
    var MESSAGE_VERSION = "Using version: %s"
    var EDITOR_NAME = "&eName"
    var EDITOR_ERROR_MODEL = "Input an integer"
    var EDITOR_ERROR_SWAP = "Must swap with an item"
    var EDITOR_EDIT_ITEM1 = "Edit item"
    fun load(@Nonnull sender: CommandSender?, @Nonnull language: String): Boolean {
        return try {
            PATH.mkdirs()
            val file = File(PATH, "$language.yml")
            val config = YamlConfiguration()
            config.load(file)
            for (key in config.getKeys(false)) {
                // get field by that name
                try {
                    val field = Lang::class.java.getDeclaredField(key)
                    if (!Modifier.isFinal(field.modifiers) && field.type == String::class.java) {
                        // in this case if the field from the config is not found, then
                        field[null] = config.getString(key)!!.replace("\\n", "\n")
                    }
                } catch (e: NoSuchFieldException) {
                    // field from config doesnt exist, meaning that
                    LCMain.Companion.get()!!.notifier!!.commandSevere(sender!!, "Unknown language tag $key")
                } catch (ignored: Exception) {
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun save(@Nonnull sender: CommandSender?, @Nonnull language: String, overwrite: Boolean) {
        try {
            val file = File(PATH, "$language.yml")
            if (overwrite || !file.exists()) {
                PATH.mkdirs()
                val config = YamlConfiguration()
                for (field in Lang::class.java.getDeclaredFields()) {
                    try {
                        if (field.type == String::class.java) {
                            config[field.name] = (field[null] as String).replace("\n", "\\n")
                        }
                    } catch (ignored: Exception) {
                    }
                }
                config.save(file)
                LCMain.Companion.get()!!.notifier!!.commandInfo(sender!!, String.format("Language file %s overwritten", language))
            } else {
                LCMain.Companion.get()!!.notifier!!.commandInfo(sender!!, String.format("Language file %s was not overwritten", language))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
