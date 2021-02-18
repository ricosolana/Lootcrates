package com.crazicrafter1.lootcrates.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ListenerOnAsyncPlayerChat extends BaseListener {

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent e){
        //e.setCancelled(true);
        /*
        Player p = e.getPlayer();
        String[] arr = e.getMessage().split(" ");
        if(!arr[0].equalsIgnoreCase("/pin")){
            return;
        }
        p.sendMessage("Test");
        e.setCancelled(true);

         */
    }

}
