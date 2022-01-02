package com.crazicrafter1.lootcrates.sk;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SkriptLootEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private String tag;
    private Player player;

    public SkriptLootEvent(String tag, Player player) {
        this.tag = tag;
        this.player = player;
    }

    public String getTag() {
        return tag;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
