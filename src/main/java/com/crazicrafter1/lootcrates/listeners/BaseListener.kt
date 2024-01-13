package com.crazicrafter1.lootcrates.listeners

import com.crazicrafter1.lootcrates.LCMain
import org.bukkit.Bukkit
import org.bukkit.event.Listener

open class BaseListener(plugin: LCMain?) : Listener {
    init {
        Bukkit.getPluginManager().registerEvents(this, plugin!!)
    }

    companion object {
        protected var plugin: LCMain? = LCMain.Companion.get()
    }
}
