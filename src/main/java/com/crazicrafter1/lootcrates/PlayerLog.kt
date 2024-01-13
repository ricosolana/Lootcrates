package com.crazicrafter1.lootcrates

import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class PlayerLog {
    // get crate open information and other stuff to analyze possible duping or whatever
    //
    private val crates = HashMap<String?, MutableList<String>>()
    fun increment(id: String?) {
        val dateList = crates.computeIfAbsent(id) { k: String? -> ArrayList() }
        val formatter = SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z")
        val date = Date(System.currentTimeMillis())
        dateList.add(formatter.format(date))
    }

    companion object {
        private val PATH: File = File(LCMain.Companion.get()!!.getDataFolder(), "players")

        //public boolean antiExploit = false;
        private val LOGS = HashMap<UUID, PlayerLog>()

        @Nonnull
        operator fun get(uuid: UUID): PlayerLog {
            // if not present add
            var stat = LOGS[uuid]
            if (stat == null) {
                stat = PlayerLog()
                LOGS[uuid] = stat
            }
            return stat
        }

        fun saveAll(@Nonnull sender: CommandSender?) {
            try {
                for ((key, value) in LOGS) {
                    val rawUUID = key.toString()
                    val playerConfig = YamlConfiguration()
                    for ((key1, value1) in value.crates) {
                        playerConfig[key1!!] = value1
                    }
                    playerConfig.save(File(PATH, "$rawUUID.yml"))
                }
            } catch (e: Exception) {
                LCMain.Companion.get()!!.notifier!!.severe(sender!!, String.format(Lang.MESSAGE_STATS_ERROR10, e.message))
            }
        }

        fun loadAll(@Nonnull sender: CommandSender?) {
            try {
                if (LCMain.Companion.get()!!.rev < 5) return
                if (!PATH.exists() || !PATH.isDirectory()) return

                // playerStatsPath.mkdirs();
                val files = PATH.listFiles() // TODO use Paths.walk
                for (file in files) {
                    try {
                        val rawUUID = file.getName().replace(".yml", "")
                        val uuid = UUID.fromString(rawUUID)
                        val playerConfig = YamlConfiguration()
                        playerConfig.load(file)
                        val stat = PlayerLog()
                        LOGS[uuid] = stat
                        for (id in playerConfig.getKeys(false)) stat.crates[id] = playerConfig.getStringList(id!!)
                    } catch (ignored: Exception) {
                    }
                }
            } catch (e: Exception) {
                LCMain.Companion.get()!!.notifier!!.severe(sender!!, String.format(Lang.MESSAGE_STATS_ERROR11, e.message))
            }
        }
    }
}
