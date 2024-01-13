package com.crazicrafter1.lootcrates

import ch.njol.skript.Skript
import ch.njol.skript.SkriptAddon
import com.crazicrafter1.crutils.*
import com.crazicrafter1.lootcrates.cmd.Cmd
import com.crazicrafter1.lootcrates.crate.loot.*
import com.crazicrafter1.lootcrates.listeners.ListenerCrateDeletion
import com.crazicrafter1.lootcrates.listeners.ListenerCrates
import com.crazicrafter1.lootcrates.listeners.ListenerOnEditorChatCommandLoot
import org.bukkit.*
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.configuration.serialization.ConfigurationSerialization
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import java.awt.Color
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.util.*
import java.util.AbstractMap.SimpleEntry
import java.util.Map
import java.util.function.Function
import java.util.regex.Pattern
import java.util.stream.Collectors
import kotlin.math.floor

class LCMain : JavaPlugin() {
    var notifier: Notifier? = null

    //private final File cratesConfigFile_REV9_ONWARD = new File(getDataFolder(), "crates.yml");
    private val rewardsConfigFile = File(dataFolder, "rewards.yml")

    //private final File lootConfigFile_REV9_ONWARD = new File(getDataFolder(), "loot.yml");
    private val configFile = File(dataFolder, "config.yml")
    private val backupPath = File(dataFolder, "backup")
    private val certsFile = File(dataFolder, "certs.yml")
    var lootClasses: MutableMap<Class<out ILoot>, ItemStack?> = HashMap()
    private var config: FileConfiguration? = null
    var supportQualityArmory = false
    var supportSkript = false
    var supportMMOItems = false
    var addon: SkriptAddon? = null
    var rewardSettings: RewardSettings? = null
    var rev = REV_LATEST
    var language = "en"
    var cleanPeriod = 30
    var debug = false
    var checkCerts = false
    override fun onEnable() {
        instance = this
        notifier = Notifier(ChatColor.WHITE.toString() + "[%sLC" + ChatColor.WHITE + "] %s%s", PERM_ADMIN)
        notifier!!.info(ColorUtil.renderAll(String.format(Lang.MESSAGE_DISCORD, DISCORD_URL)))
        try {
            Files.createDirectories(dataFolder.toPath())
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        doSplash()
        reloadConfig()
        checkUpdates()
        checkAddons()
        reloadData(Bukkit.getConsoleSender()) // must be after skript
        initMetrics()
        Cmd(this)
        ListenerCrates(this)
        ListenerCrateDeletion(this)
        ListenerOnEditorChatCommandLoot(this)
    }

    override fun onDisable() {
        this.saveConfig()
        saveOtherConfigs(Bukkit.getConsoleSender())
        deleteOldBackups(Bukkit.getConsoleSender())
    }

    override fun saveDefaultConfig() {
        saveDefaultConfig(Bukkit.getConsoleSender(), false)
    }

    override fun reloadConfig() {
        reloadConfig(Bukkit.getConsoleSender())
    }

    override fun saveConfig() {
        saveConfig(Bukkit.getConsoleSender())
    }

    private fun registerLoot(lootClass: Class<out ILoot>) {
        lootClasses[lootClass] = ReflectionUtil.getFieldInstance(ReflectionUtil.getField(lootClass, "EDITOR_ICON"), null) as ItemStack
        ConfigurationSerialization.registerClass(lootClass, lootClass.getSimpleName())
        notifier!!.info("Registering " + lootClass.getSimpleName())
    }

    private fun doSplash() {
        if (Version.AT_LEAST_v1_16.a()) {
            val c = System.currentTimeMillis()
            val mul = 1.0 / (1000.0 * 60.0)
            val h3 = c.toDouble() * mul
            val fh3 = (h3 - floor(h3)).toFloat()
            val color1 = Color.getHSBColor(fh3, .85f, .75f)
            val color2 = Color.getHSBColor(fh3 + .3f, .85f, .75f)

            // convert to hex
            val hex1 = ColorUtil.toHex(color1)
            val hex2 = ColorUtil.toHex(color2)
            val split = SPLASH.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (i in split.indices) {
                split[i] = ColorUtil.renderAll(String.format("<#%s>%s</#%s>", hex1, split[i], hex2))
            }
            val res = java.lang.String.join("\n", *split)
            Bukkit.getConsoleSender().sendMessage("""
    
    
    
    
    $res
    
    
    
    
    """.trimIndent())
        }
    }

    private fun checkUpdates() {
        GitUtils.checkForUpdateAsync(this, "PeriodicSeizures", "Lootcrates"
        ) { result: Boolean, tag: String? -> if (result) notifier!!.info(String.format(Lang.MESSAGE_UPDATE_AVAILABLE, tag)) else notifier!!.info(Lang.MESSAGE_VERSION1) }
    }

    private fun initMetrics() {
        try {
            val metrics = Metrics(this, 10395)
            metrics.addCustomChart(Metrics.SimplePie(KEY_LANGUAGE
            ) { language }
            )
            metrics.addCustomChart(Metrics.AdvancedPie("loot"
            ) {
                rewardSettings!!.lootSets!!.keys.stream().map { lootSet: String? -> SimpleEntry<String?, Int>(lootSet, 1) }.collect(Collectors.toMap({ (key, _) -> key }, { (_, value) -> value }))
            }
            )
            metrics.addCustomChart(
                    Metrics.AdvancedPie("crates"
                    ) {
                        rewardSettings!!.crates!!.keys.stream().map { crate: String? -> SimpleEntry<String?, Int>(crate, 1) }.collect(Collectors.toMap({ (key, _) -> key }, { (_, value) -> value }))
                    }
            )
            metrics.addCustomChart(
                    Metrics.SimplePie(KEY_CHECK_CERTS) { checkCerts.toString() }
            )
        } catch (e: Exception) {
            notifier!!.severe(String.format(Lang.METRICS_ERROR, e.message))
        }
    }

    private fun checkAddons() {
        supportQualityArmory = Bukkit.getPluginManager().isPluginEnabled("QualityArmory")
        supportSkript = Bukkit.getPluginManager().isPluginEnabled("Skript")
        supportMMOItems = Bukkit.getPluginManager().isPluginEnabled("MMOItems")

        // Register loot classes
        registerLoot(LootCommand::class.java)
        registerLoot(LootItem::class.java)
        registerLoot(LootItemCrate::class.java)
        if (supportQualityArmory) registerLoot(LootItemQA::class.java)
        if (supportSkript) {
            addon = Skript.registerAddon(this)
            try {
                addon.loadClasses(javaClass.getPackage().name, "sk")
            } catch (e: Exception) {
                notifier!!.severe(Lang.MESSAGE_SKRIPT_ERROR1)
                e.printStackTrace()
            }
            registerLoot(LootSkriptEvent::class.java)
        }
        if (supportMMOItems) registerLoot(LootMMOItem::class.java)
    }

    fun saveDefaultFile(sender: CommandSender?, file: File, overwrite: Boolean) {
        if (overwrite || !Files.exists(file.toPath())) {
            notifier!!.info(sender!!, String.format(Lang.MESSAGE_SAVING_DEFAULT, file.getName()))
            saveResource(file.getName(), true)
        }
    }

    fun saveDefaultConfig(sender: CommandSender?, overwrite: Boolean) {
        saveDefaultFile(sender, configFile, overwrite)
    }

    fun backupRewards(sender: CommandSender?, isBroken: Boolean): Boolean {
        val backupFile = File(backupPath, System.currentTimeMillis().toString() + "_" + (if (isBroken) "broken" else "old") + "_rewards_rev" + rev + ".zip")
        notifier!!.info(sender!!, Lang.MESSAGE_REWARDS_BACKUP)
        return Util.zip(rewardsConfigFile, backupFile)
    }

    fun reloadConfig(@Nonnull sender: CommandSender?) {
        try {
            saveDefaultConfig()
            config = YamlConfiguration.loadConfiguration(configFile)
            rev = config.getInt(KEY_rev, REV_LATEST)
            language = config.getString(KEY_LANGUAGE, language)!!
            cleanPeriod = config.getInt(KEY_CLEAN_PERIOD, cleanPeriod)
            debug = config.getBoolean(KEY_DEBUG, debug)
            checkCerts = config.getBoolean(KEY_CHECK_CERTS, checkCerts)
        } catch (e: Exception) {
            notifier!!.severe(sender!!, String.format(Lang.CONFIG_ERROR3, e.message))
        }
    }

    fun reloadData(@Nonnull sender: CommandSender?) {
        PlayerLog.Companion.loadAll(sender)

        // TODO close only open editor menus, not other possible ones
        //AbstractMenu.closeAllMenus();
        saveDefaultFile(sender, rewardsConfigFile, false)
        val rewardsConfig: FileConfiguration = YamlConfiguration.loadConfiguration(rewardsConfigFile)
        val certsConfig: FileConfiguration = YamlConfiguration.loadConfiguration(certsFile)
        crateCerts = certsConfig.getStringList(KEY_certs).stream().map { name: String? -> UUID.fromString(name) }.collect(Collectors.toSet())

        // save default en.yml file
        Lang.save(sender, "en", false)
        Lang.load(sender, language)
        try {
            rewardSettings = RewardSettings(rewardsConfig)
        } catch (e1: Exception) {
            e1.printStackTrace()
        }
        if (rewardSettings != null) {
            notifier!!.info(sender!!, Lang.MESSAGE_REWARDS_SUCCESS)
            return
        }
        notifier!!.severe(sender!!, String.format(Lang.MESSAGE_REWARDS_REPORT, DISCORD_URL))
        Bukkit.getPluginManager().disablePlugin(this)
    }

    /*
    // TODO rev 8
    //  rev 8 will have a crates.yml and rewards.yml
    //  crates.yml: contains crates with their lootCollection id references
    //  rewards.yml: contains lootItems in their respective lootCollections
    public void reloadCrateConfig(@Nonnull CommandSender sender) {
        PlayerLog.loadAll(sender);

        saveDefaultFile(sender, rewardsConfigFile, false);

        FileConfiguration rewardsConfig = YamlConfiguration.loadConfiguration(rewardsConfigFile);

        // save default en.yml file
        Lang.save(sender, "en", false);
        Lang.load(sender, language);

        try {
            this.rewardSettings = new RewardSettings(rewardsConfig);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (rewardSettings != null) {
            notifier.info(sender, Lang.REWARDS_SUCCESS);
            return;
        }

        notifier.severe(sender, Lang.REWARDS_FAIL);
        notifier.severe(sender, String.format(Lang.REWARDS_REPORT, DISCORD_URL));
        Bukkit.getPluginManager().disablePlugin(this);
    }
    */
    fun saveConfig(@Nonnull sender: CommandSender?) {
        // if a backup was successfully made, then save
        try {
            config = YamlConfiguration()
            config.set(KEY_rev, REV_LATEST)
            config.set(KEY_LANGUAGE, language)
            config.set(KEY_CLEAN_PERIOD, cleanPeriod)
            config.set(KEY_DEBUG, debug)
            config.set(KEY_CHECK_CERTS, checkCerts)
            config.save(configFile)
        } catch (e: IOException) {
            notifier!!.severe(sender!!, String.format(Lang.CONFIG_ERROR5, e.message))
            e.printStackTrace()
        }
    }

    fun saveOtherConfigs(@Nonnull sender: CommandSender?) {
        // if a backup was successfully made, then save
        PlayerLog.Companion.saveAll(sender)
        if (backupRewards(sender, false)) {
            notifier!!.info(sender!!, Lang.CONFIG_SAVING)
            val rewardsConfig: FileConfiguration = YamlConfiguration()
            rewardSettings!!.serialize(rewardsConfig)
            val certsConfig: FileConfiguration = YamlConfiguration()
            certsConfig[KEY_certs] = crateCerts.stream().map { obj: UUID -> obj.toString() }.collect(Collectors.toList())
            try {
                rewardsConfig.save(rewardsConfigFile)
                certsConfig.save(certsFile)
            } catch (e: Exception) {
                notifier!!.severe(sender, Lang.CONFIG_ERROR6)
                e.printStackTrace()
            }
        } else notifier!!.severe(sender!!, Lang.CONFIG_ERROR1)
    }

    private fun deleteOldBackups(@Nonnull sender: CommandSender) {
        if (cleanPeriod <= 0) return
        try {
            var deletedCount = 0
            Files.createDirectories(backupPath.toPath())
            for (file in backupPath.listFiles()) {
                val name = file.getName()
                val matcher = BACKUP_PATTERN.matcher(name)
                if (matcher.matches()) {
                    val create = name.substring(0, name.indexOf("_")).toLong()
                    if (create < System.currentTimeMillis() - cleanPeriod.toLong() * 24 * 60 * 60 * 1000) {
                        // delete it
                        Files.delete(file.toPath())
                        deletedCount++
                    }
                }
            }
            if (deletedCount > 0) notifier!!.info(sender, String.format(Lang.CONFIG_DELETED_OLD, deletedCount)) else notifier!!.info(sender, Lang.MESSAGE_NO_CONFIG_DELETES)
        } catch (e: Exception) {
            notifier!!.severe(sender, String.format(Lang.CONFIG_ERROR2, e.message))
        }
    }

    override fun getConfig(): FileConfiguration {
        if (config == null) {
            this.reloadConfig()
        }
        return config!!
    }

    companion object {
        val NUMBER_AT_END = Pattern.compile("\\d+$")
        private const val SPLASH = " __         ______     ______     ______   ______     ______     ______     ______   ______     ______    \n" +
                "/\\ \\       /\\  __ \\   /\\  __ \\   /\\__  _\\ /\\  ___\\   /\\  == \\   /\\  __ \\   /\\__  _\\ /\\  ___\\   /\\  ___\\   \n" +
                "\\ \\ \\____  \\ \\ \\/\\ \\  \\ \\ \\/\\ \\  \\/_/\\ \\/ \\ \\ \\____  \\ \\  __<   \\ \\  __ \\  \\/_/\\ \\/ \\ \\  __\\   \\ \\___  \\  \n" +
                " \\ \\_____\\  \\ \\_____\\  \\ \\_____\\    \\ \\_\\  \\ \\_____\\  \\ \\_\\ \\_\\  \\ \\_\\ \\_\\    \\ \\_\\  \\ \\_____\\  \\/\\_____\\ \n" +
                "  \\/_____/   \\/_____/   \\/_____/     \\/_/   \\/_____/   \\/_/ /_/   \\/_/\\/_/     \\/_/   \\/_____/   \\/_____/"
        var crateCerts: MutableSet<UUID> = HashSet() // drm
        const val REV_LATEST = 10
        const val DISCORD_URL = "https://discord.gg/K9xDtEKyXT"
        const val PERM_ADMIN = "lootcrates.admin"
        const val PERM_OPEN = "lootcrates.open"
        const val PERM_PREVIEW = "lootcrates.preview"
        const val KEY_rev = "rev"
        const val KEY_LANGUAGE = "language"
        const val KEY_CLEAN_PERIOD = "clean-period"
        const val KEY_DEBUG = "debug"
        const val KEY_CHECK_CERTS = "check-certs"
        const val KEY_certs = "certs"
        private var instance: LCMain? = null
        fun get(): LCMain {
            return instance!!
        }

        private val BACKUP_PATTERN = Pattern.compile("^([0-9])+(_\\S+)?.zip")
    }
}