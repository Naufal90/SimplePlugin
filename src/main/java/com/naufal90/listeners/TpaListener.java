package com.naufal90.listeners;

import com.naufal90.managers.TpaManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class TpaListener implements Listener {
    
    private final TpaManager tpaManager;
    
    public TpaListener(TpaManager tpaManager) {
        this.tpaManager = tpaManager;
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Remove cooldown when player leaves
        tpaManager.removeCooldown(event.getPlayer().getUniqueId());
    }
}