package com.naufal90.listeners;

import com.naufal90.managers.AuthManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class AuthListener implements Listener {
    
    private final AuthManager authManager;
    
    public AuthListener(AuthManager authManager) {
        this.authManager = authManager;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // GeyserMC compatibility check
        authManager.handleBedrockPlayerJoin(event.getPlayer());
        
        if (!authManager.isPlayerLoggedIn(event.getPlayer().getUniqueId())) {
            authManager.applyRestrictions(event.getPlayer());
            authManager.startLoginTimeout(event.getPlayer());
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        authManager.logoutPlayer(event.getPlayer());
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!authManager.isPlayerLoggedIn(event.getPlayer().getUniqueId())) {
            // Prevent movement if not logged in
            if (event.getFrom().getBlockX() != event.getTo().getBlockX() || 
                event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
                event.setTo(event.getFrom());
            }
        }
    }
    
    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage().toLowerCase();
        String[] args = message.split(" ");
        
        // Allow only auth commands when not logged in
        if (!authManager.isPlayerLoggedIn(event.getPlayer().getUniqueId())) {
            if (!args[0].equals("/login") && !args[0].equals("/register") && !args[0].equals("/l") && !args[0].equals("/reg")) {
                event.setCancelled(true);
                event.getPlayer().sendMessage("§cPlease login first using /login");
            }
        }
    }
}