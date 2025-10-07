package com.naufal90.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.UUID;
import java.lang.reflect.Method;

public class PermissionManager {
    
    private final JavaPlugin plugin;
    private boolean luckPermsEnabled;
    
    public PermissionManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.luckPermsEnabled = Bukkit.getPluginManager().isPluginEnabled("LuckPerms");
        
        if (luckPermsEnabled) {
            plugin.getLogger().info("LuckPerms detected! Using LuckPerms for permissions.");
        } else {
            plugin.getLogger().info("LuckPerms not found! Using built-in permission system.");
        }
    }
    
    public boolean hasPermission(Player player, String permission) {
        if (luckPermsEnabled) {
            // Use LuckPerms API if available
            return hasLuckPermsPermission(player, permission);
        } else {
            // Use built-in permission system
            return player.hasPermission(permission) || player.isOp();
        }
    }
    
    private boolean hasLuckPermsPermission(Player player, String permission) {
        try {
            Class<?> luckPermsClass = Class.forName("net.luckperms.api.LuckPerms");
            Object luckPerms = Bukkit.getServicesManager().load(luckPermsClass);
            if (luckPerms != null) {
                Object userManager = luckPermsClass.getMethod("getUserManager").invoke(luckPerms);
                Object user = userManager.getClass().getMethod("getUser", UUID.class).invoke(userManager, player.getUniqueId());
                if (user != null) {
                    Method getCachedData = user.getClass().getMethod("getCachedData");
                    Object cachedData = getCachedData.invoke(user);
                    Method getPermissionData = cachedData.getClass().getMethod("getPermissionData");
                    Object permissionData = getPermissionData.invoke(cachedData);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error checking LuckPerms permission: " + e.getMessage());
        }
        
        // Fallback to built-in system
        return player.hasPermission(permission) || player.isOp();
    }
    
    public boolean isAdmin(Player player) {
        return hasPermission(player, "simple.admin") || 
               hasPermission(player, "simple.*") ||
               player.isOp();
    }
    
    public String getGroup(Player player) {
        if (luckPermsEnabled) {
            try {
                Class<?> luckPermsClass = Class.forName("net.luckperms.api.LuckPerms");
                Object luckPerms = Bukkit.getServicesManager().load(luckPermsClass);
                if (luckPerms != null) {
                    Method getUserManager = luckPermsClass.getMethod("getUserManager");
                    Object userManager = getUserManager.invoke(luckPerms);
                    Method getUser = userManager.getClass().getMethod("getUser", UUID.class);
                    Object user = getUser.invoke(userManager, player.getUniqueId());
                    if (user != null) {
                        Method getPrimaryGroup = user.getClass().getMethod("getPrimaryGroup");
                        return (String) getPrimaryGroup.invoke(user);
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Error getting LuckPerms group: " + e.getMessage());
            }
        }
        return "default";
    }
    
    public boolean isLuckPermsEnabled() {
        return luckPermsEnabled;
    }
}