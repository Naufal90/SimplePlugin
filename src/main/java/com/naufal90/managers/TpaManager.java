package com.naufal90.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TpaManager {
    
    private final JavaPlugin plugin;
    private final Map<UUID, TpaRequest> tpaRequests;
    private final Map<UUID, Long> cooldowns;
    
    public TpaManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.tpaRequests = new HashMap<>();
        this.cooldowns = new HashMap<>();
    }
    
    public boolean sendTpaRequest(Player sender, Player target, boolean here) {
        // Check cooldown
        if (hasCooldown(sender.getUniqueId())) {
            long remaining = getCooldownRemaining(sender.getUniqueId());
            sender.sendMessage("§cYou must wait " + (remaining/1000) + " seconds before sending another TPA request.");
            return false;
        }
        
        UUID requestId = UUID.randomUUID();
        TpaRequest request = new TpaRequest(requestId, sender.getUniqueId(), target.getUniqueId(), here, System.currentTimeMillis());
        tpaRequests.put(target.getUniqueId(), request);
        
        // Set cooldown (30 seconds)
        cooldowns.put(sender.getUniqueId(), System.currentTimeMillis() + 30000);
        
        // Auto remove request after 60 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                TpaRequest currentRequest = tpaRequests.get(target.getUniqueId());
                if (currentRequest != null && currentRequest.getRequestId().equals(requestId)) {
                    tpaRequests.remove(target.getUniqueId());
                    sender.sendMessage("§eYour TPA request to " + target.getName() + " has expired.");
                }
            }
        }.runTaskLater(plugin, 20 * 60);
        
        sender.sendMessage("§aTPA request sent to " + target.getName() + "!");
        target.sendMessage("§6" + sender.getName() + " has sent you a TPA request! Type §a/tpaccept §6or §c/tpdeny");
        
        return true;
    }
    
    public boolean acceptTpaRequest(Player target) {
        TpaRequest request = tpaRequests.remove(target.getUniqueId());
        if (request == null) {
            target.sendMessage("§cYou don't have any pending TPA requests.");
            return false;
        }
        
        Player sender = Bukkit.getPlayer(request.getSender());
        if (sender == null || !sender.isOnline()) {
            target.sendMessage("§cThe player who sent the TPA request is no longer online.");
            return false;
        }
        
        if (request.isHere()) {
            // Tpahere - bring target to sender
            Location targetLocation = target.getLocation();
            World targetWorld = target.getWorld();
            
            // Teleport with world compatibility
            target.teleport(sender.getLocation());
            target.sendMessage("§aYou have been teleported to " + sender.getName() + "!");
            sender.sendMessage("§a" + target.getName() + " has accepted your TPA request!");
        } else {
            // Normal TPA - bring sender to target
            Location senderLocation = sender.getLocation();
            World senderWorld = sender.getWorld();
            
            // Teleport with world compatibility
            sender.teleport(target.getLocation());
            sender.sendMessage("§aYou have been teleported to " + target.getName() + "!");
            target.sendMessage("§a" + sender.getName() + " has been teleported to you!");
        }
        
        return true;
    }
    
    public boolean denyTpaRequest(Player target) {
        TpaRequest request = tpaRequests.remove(target.getUniqueId());
        if (request == null) {
            target.sendMessage("§cYou don't have any pending TPA requests.");
            return false;
        }
        
        Player sender = Bukkit.getPlayer(request.getSender());
        if (sender != null && sender.isOnline()) {
            sender.sendMessage("§cYour TPA request was denied by " + target.getName() + ".");
        }
        
        target.sendMessage("§aYou have denied the TPA request.");
        return true;
    }
    
    private boolean hasCooldown(UUID playerId) {
        return cooldowns.containsKey(playerId) && cooldowns.get(playerId) > System.currentTimeMillis();
    }
    
    private long getCooldownRemaining(UUID playerId) {
        return (cooldowns.get(playerId) - System.currentTimeMillis()) / 1000;
    }
    
    public void removeCooldown(UUID playerId) {
        cooldowns.remove(playerId);
    }
    
    private static class TpaRequest {
        private final UUID requestId;
        private final UUID sender;
        private final UUID target;
        private final boolean here;
        private final long timestamp;
        
        public TpaRequest(UUID requestId, UUID sender, UUID target, boolean here, long timestamp) {
            this.requestId = requestId;
            this.sender = sender;
            this.target = target;
            this.here = here;
            this.timestamp = timestamp;
        }
        
        public UUID getRequestId() { return requestId; }
        public UUID getSender() { return sender; }
        public UUID getTarget() { return target; }
        public boolean isHere() { return here; }
        public long getTimestamp() { return timestamp; }
    }
}