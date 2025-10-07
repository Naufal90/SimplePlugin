package com.naufal90.managers;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class RtpManager {
    
    private final JavaPlugin plugin;
    private final Random random;
    private final Map<UUID, Long> cooldowns;
    
    // RTP Configuration
    private final int minDistance;
    private final int maxDistance;
    private final int maxAttempts;
    private final long cooldownTime;
    
    public RtpManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.random = new Random();
        this.cooldowns = new HashMap<>();
        
        // Load configuration
        this.minDistance = plugin.getConfig().getInt("rtp.min-distance", 100);
        this.maxDistance = plugin.getConfig().getInt("rtp.max-distance", 1000);
        this.maxAttempts = plugin.getConfig().getInt("rtp.max-attempts", 10);
        this.cooldownTime = plugin.getConfig().getLong("rtp.cooldown", 60) * 1000; // Convert to milliseconds
    }
    
    public boolean randomTeleport(Player player) {
        // Check cooldown
        if (hasCooldown(player.getUniqueId())) {
            long remaining = getCooldownRemaining(player.getUniqueId());
            player.sendMessage("§cYou must wait " + remaining + " seconds before using RTP again.");
            return false;
        }
        
        World world = player.getWorld();
        
        // Find safe location
        Location safeLocation = findSafeLocation(world);
        if (safeLocation == null) {
            player.sendMessage("§cCould not find a safe location to teleport you. Please try again.");
            return false;
        }
        
        // Teleport player
        player.teleport(safeLocation);
        player.sendMessage("§aYou have been randomly teleported!");
        
        // Set cooldown
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + cooldownTime);
        
        return true;
    }
    
    private Location findSafeLocation(World world) {
        for (int i = 0; i < maxAttempts; i++) {
            // Generate random coordinates
            int x = random.nextInt(maxDistance - minDistance) + minDistance;
            int z = random.nextInt(maxDistance - minDistance) + minDistance;
            
            // Randomize positive/negative
            if (random.nextBoolean()) x = -x;
            if (random.nextBoolean()) z = -z;
            
            // Get highest block at location
            int y = world.getHighestBlockYAt(x, z);
            Location location = new Location(world, x + 0.5, y + 1, z + 0.5);
            
            // Check if location is safe
            if (isSafeLocation(location)) {
                return location;
            }
        }
        
        return null;
    }
    
    private boolean isSafeLocation(Location location) {
        World world = location.getWorld();
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        
        // Check if block below is solid
        Material below = world.getBlockAt(x, y - 1, z).getType();
        if (below == Material.AIR || below == Material.WATER || below == Material.LAVA) {
            return false;
        }
        
        // Check if current position and above are safe
        Material current = world.getBlockAt(x, y, z).getType();
        Material above = world.getBlockAt(x, y + 1, z).getType();
        
        return current == Material.AIR && above == Material.AIR;
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
}