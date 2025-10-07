package com.naufal90.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class WarpManager {
    
    private final JavaPlugin plugin;
    private final File warpsFile;
    private FileConfiguration warpsConfig;
    private final Map<String, Location> warps;
    private final Map<String, String> warpPermissions;
    
    public WarpManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.warpsFile = new File(plugin.getDataFolder(), "warps.yml");
        this.warps = new HashMap<>();
        this.warpPermissions = new HashMap<>();
        
        if (!warpsFile.exists()) {
        try {
            warpsFile.getParentFile().mkdirs();
            warpsFile.createNewFile();
        } catch (IOException e) {
            plugin.getLogger().severe("Could not create warps.yml: " + e.getMessage());
        }
    }
        loadWarps();
    }
    
    public void loadWarps() {
        if (!warpsFile.exists()) {
            plugin.saveResource("warps.yml", false);
        }
        
        warpsConfig = YamlConfiguration.loadConfiguration(warpsFile);
        warps.clear();
        warpPermissions.clear();
        
        if (warpsConfig.contains("warps")) {
            for (String warpName : warpsConfig.getConfigurationSection("warps").getKeys(false)) {
                String path = "warps." + warpName;
                
                String worldName = warpsConfig.getString(path + ".world");
                double x = warpsConfig.getDouble(path + ".x");
                double y = warpsConfig.getDouble(path + ".y");
                double z = warpsConfig.getDouble(path + ".z");
                float yaw = (float) warpsConfig.getDouble(path + ".yaw", 0.0);
                float pitch = (float) warpsConfig.getDouble(path + ".pitch", 0.0);
                String permission = warpsConfig.getString(path + ".permission", "simple.warp." + warpName.toLowerCase());
                
                World world = Bukkit.getWorld(worldName);
                if (world != null) {
                    Location location = new Location(world, x, y, z, yaw, pitch);
                    warps.put(warpName.toLowerCase(), location);
                    warpPermissions.put(warpName.toLowerCase(), permission);
                } else {
                    plugin.getLogger().warning("World '" + worldName + "' for warp '" + warpName + "' not found!");
                }
            }
        }
    }
    
    public boolean setWarp(Player player, String name, Location location) {
      PermissionManager permManager = ((com.naufal90.SimplePlugin) plugin).getPermissionManager();
        if (!permManager.hasPermission(player, "simple.setwarp") && !permManager.isAdmin(player)) {
            player.sendMessage("§cYou don't have permission to set warps!");
            return false;
        }
        
        String lowerName = name.toLowerCase();
        warps.put(lowerName, location);
        warpPermissions.put(lowerName, "simple.warp." + lowerName);
        saveWarps();
        return true;
    }
    
    public boolean deleteWarp(Player player, String name) {
      PermissionManager permManager = ((com.naufal90.SimplePlugin) plugin).getPermissionManager();
        if (!permManager.hasPermission(player, "simple.delwarp") && !permManager.isAdmin(player)) {
            player.sendMessage("§cYou don't have permission to delete warps!");
            return false;
        }
        String lowerName = name.toLowerCase();
        if (warps.containsKey(lowerName)) {
            warps.remove(lowerName);
            warpPermissions.remove(lowerName);
            saveWarps();
            return true;
        }
        return false;
    }
    
    public Location getWarp(String name) {
        return warps.get(name.toLowerCase());
    }
    
    public String getWarpPermission(String name) {
        return warpPermissions.get(name.toLowerCase());
    }
    
    public List<String> getWarpNames() {
        return new ArrayList<>(warps.keySet());
    }
    
    public List<String> getAccessibleWarps(Player player) {
        List<String> accessible = new ArrayList<>();
        PermissionManager permManager = ((com.naufal90.SimplePlugin) plugin).getPermissionManager();
        
        for (String warpName : warps.keySet()) {
            String permission = warpPermissions.get(warpName);
            if (permission == null || permManager.hasPermission(player, permission) || 
                permManager.hasPermission(player, "simple.warp.*")) {
                accessible.add(warpName);
            }
        }
        
        return accessible;
    }
    
    public boolean teleportToWarp(Player player, String warpName) {
        Location warp = getWarp(warpName.toLowerCase());
        if (warp == null) {
            return false;
        }
        
        String permission = getWarpPermission(warpName);
        PermissionManager permManager = ((com.naufal90.SimplePlugin) plugin).getPermissionManager();
        
        if (permission != null && !permManager.hasPermission(player, permission) && 
            !permManager.hasPermission(player, "simple.warp.*")) {
            player.sendMessage("§cYou don't have permission to use this warp!");
            return false;
        }
        
        // Check if world exists and is loaded
        World world = warp.getWorld();
        if (world == null) {
            player.sendMessage("§cThe world for this warp is not available!");
            return false;
        }
        
        player.teleport(warp);
        player.sendMessage("§aTeleported to warp: §e" + warpName);
        return true;
    }
    
    private void saveWarps() {
        warpsConfig.set("warps", null);
        
        for (Map.Entry<String, Location> entry : warps.entrySet()) {
            String path = "warps." + entry.getKey();
            Location loc = entry.getValue();
            String permission = warpPermissions.get(entry.getKey());
            
            warpsConfig.set(path + ".world", loc.getWorld().getName());
            warpsConfig.set(path + ".x", loc.getX());
            warpsConfig.set(path + ".y", loc.getY());
            warpsConfig.set(path + ".z", loc.getZ());
            warpsConfig.set(path + ".yaw", loc.getYaw());
            warpsConfig.set(path + ".pitch", loc.getPitch());
            warpsConfig.set(path + ".permission", permission);
        }
        
        try {
            warpsConfig.save(warpsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save warps.yml: " + e.getMessage());
        }
    }
}