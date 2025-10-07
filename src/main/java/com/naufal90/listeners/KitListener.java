package com.naufal90.listeners;

import com.naufal90.managers.KitManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class KitListener implements Listener {
    
    private final KitManager kitManager;
    
    public KitListener(KitManager kitManager) {
        this.kitManager = kitManager;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Give first join kits if configured
        if (!event.getPlayer().hasPlayedBefore()) {
            kitManager.giveFirstJoinKits(event.getPlayer());
        }
    }
}